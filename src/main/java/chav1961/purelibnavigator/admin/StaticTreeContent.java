package chav1961.purelibnavigator.admin;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;

public class StaticTreeContent extends JTree {
	private static final long 				serialVersionUID = 1L;
	private static final long				TT_DELAY = 500;
	
	private static final String				CONTENT_FILE = "content.json";
	private static final String				F_ID = "id";
	private static final String				F_TYPE = "type";
	private static final String				F_NAME = "name";
	private static final String				F_CAPTION = "caption";
	private static final String				F_CONTENT = "content";
	
	private static final String				AC_CUT;
	private static final String				AC_COPY;
	private static final String				AC_PASTE;

	private static final DataFlavor[]		FLAVORS;
	
	
	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final FileSystemInterface		fsi;
	private final TreeSelectionCallback		callback;
	private final JPopupMenu				nodeMenu; 
	private final JPopupMenu				leafMenu; 
	private final JPopupMenu				emptyMenu; 
	
	private DefaultMutableTreeNode			lastPopupItem;
	private JsonNode						lastPopupNode;
	private Cursor							oldCursor;
	private TimerTask						tt = null;
	private boolean							dragged = false;
	private JComponent 						focusOwner = null;
	
	public interface TreeSelectionCallback {
		void process(DefaultMutableTreeNode item, JsonNode node);
	}
	
	static {
		FLAVORS = new DataFlavor[]{new DataFlavor(JsonNode.class, "JSON NODE")};
		AC_CUT = TransferHandler.getCutAction().getValue(Action.NAME).toString();
		AC_COPY = TransferHandler.getCopyAction().getValue(Action.NAME).toString();
		AC_PASTE = TransferHandler.getPasteAction().getValue(Action.NAME).toString();
	}
	
	public StaticTreeContent(final ContentMetadataInterface mdi, final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final TreeSelectionCallback callback) throws ContentException, ClassNotFoundException {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (fsi == null) {
			throw new NullPointerException("File system can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Tree selection callback can't be null"); 
		}
		else {
			this.mdi = mdi;
			this.localizer = localizer;
			this.logger = logger;
			this.fsi = fsi;
			this.callback = callback;
			this.nodeMenu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.rightNodeMenu")), JPopupMenu.class);
			this.leafMenu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.rightLeafMenu")), JPopupMenu.class);
			this.emptyMenu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.rightEmptyMenu")), JPopupMenu.class);
			SwingUtils.assignActionListeners(this.nodeMenu,this);
			SwingUtils.assignActionListeners(this.leafMenu,this);
			SwingUtils.assignActionListeners(this.emptyMenu,this);
			
			try(final FileSystemInterface		content = fsi.clone().open("/"+CONTENT_FILE)) {
				
				if (content.exists() && content.isFile()) {
					try(final Reader			rdr = content.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING);
						final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
						
						parser.next();
						((DefaultTreeModel)getModel()).setRoot(buildContentTree(JsonUtils.loadJsonTree(parser), new ArrayList<>(), new StringBuilder()));
					}
				}
				else {
					throw new ContentException("File system ["+fsi.getAbsoluteURI()+"] doesn't contain mandatory file ["+CONTENT_FILE+"] at the root"); 
				}
			} catch (IOException e) {
				throw new ContentException("I/O error reading content descriptor: "+e.getLocalizedMessage(),e); 
			}

			final TransferHandler	th = new StaticContentTransferHandler(); 
			
			setTransferHandler(th);
			setDropMode(DropMode.ON);
			setDragEnabled(true);

			KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", (e)->{
				final Object 	o = e.getNewValue();
				
				if (o instanceof JComponent) {
					focusOwner = (JComponent)o;
				} else {
					focusOwner = null;
				}
			});
			
			final ActionListener	al = (e) -> {
				if (focusOwner == StaticTreeContent.this) {
					processCCP(th,e);
				}
			};
			
			SwingUtils.assignActionKey(this, KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), al, AC_CUT);
			SwingUtils.assignActionKey(this, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), al, AC_COPY);
			SwingUtils.assignActionKey(this, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), al, AC_PASTE);
			
			addMouseListener(new MouseListener() {
				@Override 
				public void mouseExited(final MouseEvent e) {
//					if (dragged) {
//						setCursor(DragSource.DefaultMoveNoDrop);
//					}
				}
				
				@Override 
				public void mouseEntered(final MouseEvent e) {
//					if (dragged) {
//						setCursor(DragSource.DefaultMoveDrop);
//					}
				}

				@Override 
				public void mousePressed(final MouseEvent e) {
//					final TreePath	item = getPathForLocation(e.getX(), e.getY());
//					
//					if (item != null) {
//						lastPopupItem = ((DefaultMutableTreeNode)item.getLastPathComponent());
//						lastPopupNode = (JsonNode)lastPopupItem.getUserObject();
//					}
//					dragged = false;
				}
				
				@Override 
				public void mouseReleased(final MouseEvent e) {
//					if (dragged) {
//						final TreePath	item = getPathForLocation(e.getX(), e.getY());
//						
//						if (item != null) {
//							processDragAndDrop(lastPopupItem, lastPopupNode, ((DefaultMutableTreeNode)item.getLastPathComponent()), (JsonNode)lastPopupItem.getUserObject());
//						}
//						dragged = false;
//						setCursor(oldCursor);
//					}
				}
				
				@Override
				public void mouseClicked(final MouseEvent e) {
					final TreePath	item = getPathForLocation(e.getX(), e.getY());
					final JsonNode	node = item != null ? (JsonNode) ((DefaultMutableTreeNode)item.getLastPathComponent()).getUserObject() : null;
					
					switch (e.getButton()) {
						case MouseEvent.BUTTON1 :
							if (e.getClickCount() >= 2) {
								showSettings((DefaultMutableTreeNode)item.getLastPathComponent(), node);
							}
							break;
						case MouseEvent.BUTTON2 :
							break;
						case MouseEvent.BUTTON3 :
							if (node == null) {
								lastPopupItem = null;
								lastPopupNode = null;
								showPopup(e.getPoint(), emptyMenu);
							}
							else {
								lastPopupItem = ((DefaultMutableTreeNode)item.getLastPathComponent());
								lastPopupNode = node;
								showPopup(e.getPoint(), node.hasName(F_CONTENT) ? nodeMenu : leafMenu);
							}
							break;
					}
				}
			});
			addMouseMotionListener(new MouseMotionListener() {
				@Override public void mouseMoved(final MouseEvent e) {}
				
				@Override 
				public void mouseDragged(final MouseEvent e) {
//					if (!dragged) {
//						oldCursor = getCursor();
//						setCursor(DragSource.DefaultMoveDrop);
//					}
//					dragged = true;
				}
			});
			addKeyListener(new KeyListener() {
				@Override public void keyTyped(final KeyEvent e) {}
				@Override public void keyReleased(final KeyEvent e) {}
				
				@Override
				public void keyPressed(final KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_CONTEXT_MENU :
							final TreePath	path = getSelectionPath();
							
							if (path == null) {
								final Rectangle	bounds = getVisibleRect();
								final Point		point = new Point(bounds.x+bounds.width/2, bounds.y+bounds.height/2);
								
								lastPopupItem = null;
								lastPopupNode = null;
								showPopup(point, emptyMenu);
							}
							else {
								final JsonNode	node = (JsonNode) ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
								final Rectangle	bounds = getPathBounds(path);
								final Point		point = new Point(bounds.x+bounds.width/2, bounds.y+bounds.height/2);
										
								lastPopupItem = ((DefaultMutableTreeNode)path.getLastPathComponent());
								lastPopupNode = node;
								showPopup(point, node.hasName(F_CONTENT) ? nodeMenu : leafMenu);
							}
							break;
						case KeyEvent.VK_ENTER :
							final TreePath	pathEnter = getSelectionPath();
							
							if (pathEnter != null) {
								final DefaultMutableTreeNode	item = (DefaultMutableTreeNode)pathEnter.getLastPathComponent(); 
								final JsonNode					node = (JsonNode) (item).getUserObject();
								
								showSettings(item, node);
							}
							break;
					}
				}
			});
			getSelectionModel().addTreeSelectionListener((e)->{
				if (tt != null) {
					tt.cancel();
					tt = null;
				}
				tt = new TimerTask() {
					@Override
					public void run() {
						final TreePath	path = e.getPath();
						
						if (path != null) {
							final DefaultMutableTreeNode	item = (DefaultMutableTreeNode)path.getLastPathComponent(); 
							final JsonNode					node = (JsonNode) (item).getUserObject();
							
							callback.process(item, node);
						}
						else {
							callback.process(null, null);
						}
					}
				};
				PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, TT_DELAY);
			});
			
			setRootVisible(true);
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			setCellRenderer(new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					final JLabel	label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

					label.setText(((JsonNode)((DefaultMutableTreeNode)value).getUserObject()).getChild(F_NAME).getStringValue());
					return label;
				}
			});
		}
	}

	
	private void processCCP(final TransferHandler th, final ActionEvent ae) {
		if (AC_CUT.equals(ae.getActionCommand())) {
			th.getCutAction().actionPerformed(ae);
		}
		else if (AC_COPY.equals(ae.getActionCommand())) {
			th.getCopyAction().actionPerformed(ae);
		}
		else if (AC_PASTE.equals(ae.getActionCommand())) {
			th.getPasteAction().actionPerformed(ae);
		}
		else {
			logger.message(Severity.error, "Unknown action command ["+ae.getActionCommand()+"]");
		}
	}
	
	private void processDragAndDrop(final boolean move, final DefaultMutableTreeNode fromItem, final JsonNode fromNode, final DefaultMutableTreeNode toItem, final JsonNode toNode) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(this, "drag: "+fromNode+" to "+toNode+" with move="+move);
	}

	private void showPopup(final Point point, final JPopupMenu popup) {
		popup.show(this, point.x, point.y);
	}

	private void showSettings(final DefaultMutableTreeNode item, final JsonNode node) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(this, node);
	}

	@OnAction("action:/clear")
	private void clear() {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(this, "clear: "+lastPopupNode);
	}
	
	static DefaultMutableTreeNode buildContentTree(final JsonNode node, final List<JsonNode> path, final StringBuilder sb) throws ContentException {
		path.add(node);
		if (node.getType() == JsonNodeType.JsonObject) {
			if (JsonUtils.checkJsonMandatories(node, sb, F_ID, F_TYPE, F_NAME, F_CAPTION)) {
				if (JsonUtils.checkJsonFieldTypes(node, sb, F_ID+"/"+JsonUtils.JSON_TYPE_STR, F_TYPE+"/"+JsonUtils.JSON_TYPE_STR, F_NAME+"/"+JsonUtils.JSON_TYPE_STR, F_CAPTION+"/"+JsonUtils.JSON_TYPE_STR, F_CONTENT+"/"+JsonUtils.JSON_TYPE_ARR)) {
					final DefaultMutableTreeNode	treeItem = new DefaultMutableTreeNode(node);

					if (node.hasName(F_CONTENT)) {
						for (JsonNode item : node.getChild(F_CONTENT).children()) {
							treeItem.add(buildContentTree(item, path, sb));
						}
					}
					path.remove(path.size()-1);
					return treeItem;
				}
				else {
					throw new ContentException("Illegal field(s) types ["+sb+"] at "+JsonUtils.printJsonPath(path));
				}
			}
			else {
				throw new ContentException("Mandatory field(s) ["+sb+"] is/are missing at "+JsonUtils.printJsonPath(path));
			}
		}
		else {
			throw new ContentException("Illegal JSON content format at "+JsonUtils.printJsonPath(path));
		}
	}

	private class StaticContentTransferHandler extends TransferHandler {
		private static final long 	serialVersionUID = 1L;
		private int					action;

		public boolean canImport(TransferHandler.TransferSupport info) {
	    	if (info.isDataFlavorSupported(FLAVORS[0])) {
		    	final Component	c = info.getComponent();
		    	
		    	if (c instanceof JTree) {
		    		final Point		point = info.getDropLocation().getDropPoint();
		    		final TreePath	path = ((JTree)c).getPathForLocation(point.x, point.y);
		    		
		    		if (path != null) {
		    			return ((JsonNode)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject()).hasName(F_CONTENT);
		    		}
		    		else {
		    			return false;
		    		}
		    	}
		    	else {
		    		return false;
		    	}
	    	}
	    	else {
	    		return false;
	    	}
		}
	 
		protected Transferable createTransferable(final JComponent c) {
			final TreePath	path = ((JTree)c).getSelectionPath();
			
			if (path != null) {
			    return new MyTransferable(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject());
			}
			else {
				return null;
			}
		}
		
	    public int getSourceActions(final JComponent c) {
	        return TransferHandler.COPY | TransferHandler.MOVE;
	    }
	     
	    public boolean importData(final TransferHandler.TransferSupport info) {
	        if (!info.isDrop()) {
	    		try{final TreePath					path = ((JTree)info.getComponent()).getSelectionPath();
		    		final Transferable				t = info.getTransferable();
					final JsonNode					fromNode = (JsonNode) t.getTransferData(FLAVORS[0]);
					final DefaultMutableTreeNode	toItem = (DefaultMutableTreeNode)path.getLastPathComponent();  
					final JsonNode					toNode = (JsonNode) (toItem).getUserObject();
					
			        processDragAndDrop(action == MOVE, toItem, fromNode, toItem, toNode);
			        return true;
				} catch (UnsupportedFlavorException | IOException e) {
					return false;
				}
	        }
	        else {
	    		try{final Point						point = info.getDropLocation().getDropPoint();
		    		final TreePath					path = ((JTree)info.getComponent()).getPathForLocation(point.x, point.y);
		    		final Transferable				t = info.getTransferable();
					final JsonNode					fromNode = (JsonNode) t.getTransferData(FLAVORS[0]);
					final DefaultMutableTreeNode	toItem = (DefaultMutableTreeNode)path.getLastPathComponent();  
					final JsonNode					toNode = (JsonNode) (toItem).getUserObject();
					
			        processDragAndDrop(action == MOVE, toItem, fromNode, toItem, toNode);
			        return true;
				} catch (UnsupportedFlavorException | IOException e) {
					return false;
				}
	        }
	    }

	    @Override
	    protected void exportDone(final JComponent source, final Transferable data, final int action) {
	    	this.action = action;
	    	super.exportDone(source, data, action);
	    }
	}

	private static class MyTransferable implements Transferable, ClipboardOwner {
		private final Object		value;

		public MyTransferable(final Object value) {
			this.value = value;
		}
		
		@Override
		public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
			// TODO Auto-generated method stub
			System.err.println("Looze...");
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return FLAVORS;
		}

		@Override
		public boolean isDataFlavorSupported(final DataFlavor flavor) {
			return FLAVORS[0].equals(flavor);
		}

		@Override
		public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(flavor)) {
				return value;
			}
			else {
				return null;
			}
		}
	}

	public class TransferActionListener implements ActionListener, PropertyChangeListener {
		private JComponent 				focusOwner = null;
		
		public TransferActionListener() {
			final KeyboardFocusManager 	manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			
			manager.addPropertyChangeListener("permanentFocusOwner", this);
		}
		
		public void propertyChange(final PropertyChangeEvent e) {
			final Object 	o = e.getNewValue();
			
			if (o instanceof JComponent) {
				focusOwner = (JComponent)o;
			} else {
				focusOwner = null;
			}
		}
		
		public void actionPerformed(final ActionEvent e) {
			if (focusOwner == null) {
				return;
			}
			else {
				final String 	action = (String)e.getActionCommand();
				final Action 	a = focusOwner.getActionMap().get(action);
				
				if (a != null) {
					a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
				}
			}
		}
	}	
}
