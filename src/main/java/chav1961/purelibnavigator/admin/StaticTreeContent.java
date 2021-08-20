package chav1961.purelibnavigator.admin;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;
import chav1961.purelibnavigator.interfaces.ContentNodeType;

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

	private static final String				REMOVE_LEAF_TITLE = "chav1961.purelibnavigator.admin.StaticTreeContent.removeLeaf.title";
	private static final String				REMOVE_LEAF_MESSAGE = "chav1961.purelibnavigator.admin.StaticTreeContent.removeLeaf.message";
	private static final String				REMOVE_SUBTREE_TITLE = "chav1961.purelibnavigator.admin.StaticTreeContent.removeSubtree.title";
	private static final String				REMOVE_SUBTREE_MESSAGE = "chav1961.purelibnavigator.admin.StaticTreeContent.removeSubtree.message";
	
	private static final DataFlavor[]		FLAVORS;
	
	
	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final FileSystemInterface		fsi;
	private final TreeSelectionCallback		callback;
	private final TransferHandler			th = new StaticContentTransferHandler(); 
	private final JPopupMenu				nodeMenu; 
	private final JPopupMenu				leafMenu; 
	private final JPopupMenu				emptyMenu; 
	private final NodeSettings				ns;
	private final AutoBuiltForm<NodeSettings>	form;
	
	private int								uniqueNameSuffix = 1;
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
	
	public StaticTreeContent(final ContentMetadataInterface mdi, final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final TreeSelectionCallback callback) throws ContentException, ClassNotFoundException, LocalizationException {
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
			
			SwingUtils.assignActionKey(this, SwingUtils.KS_CUT, al, AC_CUT);
			SwingUtils.assignActionKey(this, SwingUtils.KS_COPY, al, AC_COPY);
			SwingUtils.assignActionKey(this, SwingUtils.KS_PASTE, al, AC_PASTE);
			SwingUtils.assignActionKey(this, SwingUtils.KS_DELETE, (e)->{
				final ItemAndNode	sel = getSelection();
				
				if (sel != null) {
					if (sel.node.hasName(F_CONTENT)) {
						nodeRemoveSubtree();
					}
					else {
						leafRemove();
					}
				}
			}, SwingUtils.ACTION_DELETE);
			SwingUtils.assignActionKey(this, SwingUtils.KS_INSERT, (e)->{
				final ItemAndNode	sel = getSelection();
				
				if (sel != null && sel.node.hasName(F_CONTENT)) {
					nodeInsertChild();
				}
			}, SwingUtils.ACTION_INSERT);
			SwingUtils.assignActionKey(this, SwingUtils.KS_DUPLICATE, (e)->{
				final ItemAndNode	sel = getSelection();
				
				if (sel != null && !sel.node.hasName(F_CONTENT)) {
					leafDuplicate();
				}
			}, SwingUtils.ACTION_DUPLICATE);
			
			addMouseListener(new MouseListener() {
				@Override public void mouseExited(final MouseEvent e) {}
				@Override public void mouseEntered(final MouseEvent e) {}
				@Override public void mousePressed(final MouseEvent e) {}
				@Override public void mouseReleased(final MouseEvent e) {}
				
				@Override
				public void mouseClicked(final MouseEvent e) {
					final ItemAndNode	sel = getSelection(e.getPoint());
					
					switch (e.getButton()) {
						case MouseEvent.BUTTON1 :
							if (sel != null && e.getClickCount() >= 2) {
								showSettings(sel.item, sel.node);
							}
							break;
						case MouseEvent.BUTTON2 :
							break;
						case MouseEvent.BUTTON3 :
							if (sel == null) {
								lastPopupItem = null;
								lastPopupNode = null;
								showPopup(e.getPoint(), emptyMenu);
							}
							else {
								lastPopupItem = sel.item;
								lastPopupNode = sel.node;
								showPopup(e.getPoint(), sel.node.hasName(F_CONTENT) ? nodeMenu : leafMenu);
							}
							break;
					}
				}
			});
			addKeyListener(new KeyListener() {
				@Override public void keyTyped(final KeyEvent e) {}
				@Override public void keyReleased(final KeyEvent e) {}
				
				@Override
				public void keyPressed(final KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_CONTEXT_MENU :
							final ItemAndNode	sel = getSelection();
							
							if (sel == null) {
								lastPopupItem = null;
								lastPopupNode = null;
								showPopup(getRectCenter(getVisibleRect()), emptyMenu);
							}
							else {
								lastPopupItem = sel.item;
								lastPopupNode = sel.node;
								showPopup(getRectCenter(getPathBounds(getSelectionPath())), sel.node.hasName(F_CONTENT) ? nodeMenu : leafMenu);
							}
							break;
						case KeyEvent.VK_ENTER :
							final ItemAndNode	selEnter = getSelection();
							
							if (selEnter != null) {
								showSettings(selEnter.item, selEnter.node);
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
						final ItemAndNode	sel = getSelection();
						
						if (sel != null) {
							callback.process(sel.item, sel.node);
						}
						else {
							callback.process(null, null);
						}
					}
				};
				PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, TT_DELAY);
			});

			this.ns = new NodeSettings(logger);
			this.form = new AutoBuiltForm<NodeSettings>(ContentModelFactory.forAnnotatedClass(NodeSettings.class), localizer, PureLibSettings.INTERNAL_LOADER, ns, ns);
			this.form.setPreferredSize(new Dimension(300,120));
			
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
			ToolTipManager.sharedInstance().registerComponent(this);
		}
	}

	@Override
	public String getToolTipText(final MouseEvent event) {
		final ItemAndNode	sel = getSelection(event.getPoint());
		
		if (sel != null) {
			return sel.node.getChild(F_CAPTION).getStringValue();
		}
		else {
			return super.getToolTipText(event);
		}
	}

	protected void insertSibling(final DefaultMutableTreeNode parentItem, final JsonNode parentNode, final JsonNode newNode) {
		// TODO Auto-generated method stub
//		((DefaultTreeModel)getModel()).nodeStructureChanged(parentItem);
	}
	
	protected void insertChild(final DefaultMutableTreeNode parentItem, final JsonNode parentNode, final JsonNode newNode) {
		// TODO Auto-generated method stub
//		((DefaultTreeModel)getModel()).nodeStructureChanged(parentItem);
	}

	protected void removeItem(final DefaultMutableTreeNode item, final JsonNode node) {
		// TODO Auto-generated method stub
//		((DefaultTreeModel)getModel()).nodeStructureChanged(parentItem);
	}
	
	private void processCCP(final TransferHandler th, final ActionEvent ae) {
		if (AC_CUT.equals(ae.getActionCommand())) {
			TransferHandler.getCutAction().actionPerformed(ae);
		}
		else if (AC_COPY.equals(ae.getActionCommand())) {
			TransferHandler.getCopyAction().actionPerformed(ae);
		}
		else if (AC_PASTE.equals(ae.getActionCommand())) {
			TransferHandler.getPasteAction().actionPerformed(ae);
		}
		else {
			logger.message(Severity.error, "Unknown action command ["+ae.getActionCommand()+"]");
		}
	}
	
	private void showPopup(final Point point, final JPopupMenu popup) {
		popup.show(this, point.x, point.y);
	}

	@OnAction("action:/nodeCut")
	private void nodeCut() {
		TransferHandler.getCutAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, AC_CUT));
	}
	
	@OnAction("action:/nodeCopy")
	private void nodeCopy() {
		TransferHandler.getCopyAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, AC_COPY));
	}

	@OnAction("action:/nodePaste")
	private void nodePaste() {
		TransferHandler.getPasteAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, AC_PASTE));
	}

	@OnAction("action:/nodeInsertSibling")
	private void nodeInsertSibling() {
		final ItemAndNode	sel = getSelection();
		
		if (sel != null) {
			final int	suffix = uniqueNameSuffix++;
			
			ns.type = ContentNodeType.valueOf(sel.node.getChild(F_TYPE).getStringValue());
			ns.name = sel.node.getChild(F_NAME).getStringValue()+suffix;
			ns.caption = sel.node.getChild(F_CAPTION).getStringValue()+suffix;
			
			try{if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
					ns.id = UUID.randomUUID().toString();
					insertSibling(sel.item, sel.node, new JsonNode(JsonNodeType.JsonObject 
							, new JsonNode(ns.id).setName(F_ID)
							, new JsonNode(ns.type.name()).setName(F_TYPE)
							, new JsonNode(ns.name).setName(F_NAME)
							, new JsonNode(ns.caption).setName(F_CAPTION)
					));
				}
			} catch (LocalizationException e) {
			}
		}
	}

	@OnAction("action:/nodeInsertChild")
	private void nodeInsertChild() {
		final ItemAndNode	sel = getSelection();
		
		if (sel != null) {
			final int	suffix = uniqueNameSuffix++;
			
			ns.type = ContentNodeType.UNKNOWN;
			ns.name = "name"+suffix;
			ns.caption = "caption"+suffix;
			
			try{if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
					ns.id = UUID.randomUUID().toString();
					insertChild(sel.item, sel.node, new JsonNode(JsonNodeType.JsonObject 
							, new JsonNode(ns.id).setName(F_ID)
							, new JsonNode(ns.type.name()).setName(F_TYPE)
							, new JsonNode(ns.name).setName(F_NAME)
							, new JsonNode(ns.caption).setName(F_CAPTION)
					));
				}
			} catch (LocalizationException e) {
			}
		}
	}

	@OnAction("action:/nodeRemoveSubtree")
	private void nodeRemoveSubtree() {
		try{final ItemAndNode	sel = getSelection();
			
			if (sel != null && new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(REMOVE_SUBTREE_MESSAGE, sel.node.getChild(F_NAME).getStringValue()), REMOVE_SUBTREE_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				
			}
		} catch (LocalizationException e) {
		}
	}
	
	@OnAction("action:/nodeProperties")
	private void nodeProperties() {
		final ItemAndNode	sel = getSelection();
		
		if (sel != null) {
			showSettings(sel.item, sel.node);
		}
	}
	
	@OnAction("action:/leafCut")
	private void leafCut() {
		TransferHandler.getCutAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, AC_CUT));
	}
	
	@OnAction("action:/leafCopy")
	private void leafCopy() {
		TransferHandler.getCopyAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, AC_COPY));
	}

	@OnAction("action:/leafPaste")
	private void leafPaste() {
		TransferHandler.getPasteAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, AC_PASTE));
	}

	@OnAction("action:/leafDuplicate")
	private void leafDuplicate() {
		final ItemAndNode	sel = getSelection();
		
		if (sel != null) {
			final int	suffix = uniqueNameSuffix++;
			
			try{ns.type = ContentNodeType.valueOf(sel.node.getChild(F_TYPE).getStringValue());
				ns.name = sel.node.getChild(F_NAME).getStringValue()+suffix;
				ns.caption = sel.node.getChild(F_CAPTION).getStringValue()+suffix;
				
				if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
					ns.id = UUID.randomUUID().toString();
					
					final JsonNode	newNode = new JsonNode(JsonNodeType.JsonObject 
														, new JsonNode(UUID.randomUUID().toString()).setName(F_ID)
														, new JsonNode(ContentNodeType.LEAF.toString()).setName(F_TYPE)
														, new JsonNode(ns.name).setName(F_NAME)
														, new JsonNode(ns.caption).setName(F_CAPTION)
														);
					insertChild((DefaultMutableTreeNode)sel.item.getParent(),(JsonNode)((DefaultMutableTreeNode)sel.item.getParent()).getUserObject(),newNode);
				}
			} catch (LocalizationException exc) {
				logger.message(Severity.error, "Error showing settings: "+exc.getLocalizedMessage(), exc);
			}
		}
		
		JOptionPane.showMessageDialog(this, "duplicate leaf");
	}

	@OnAction("action:/leafRemove")
	private void leafRemove() {
		try{final ItemAndNode	sel = getSelection();
			
			if (sel != null && new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(REMOVE_LEAF_MESSAGE, sel.node.getChild(F_NAME).getStringValue()), REMOVE_LEAF_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				removeItem(sel.item, sel.node);
			}
		} catch (LocalizationException e) {
		}
	}

	@OnAction("action:/leafProperties")
	private void leafProperties() {
		final ItemAndNode	selEnter = getSelection();
		
		if (selEnter != null) {
			showSettings(selEnter.item, selEnter.node);
		}
	}
	
	private void processDragAndDrop(final boolean move, final DefaultMutableTreeNode fromItem, final JsonNode fromNode, final DefaultMutableTreeNode toItem, final JsonNode toNode) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(this, "drag: "+fromNode+" to "+toNode+" with move="+move);
	}

	private void showSettings(final DefaultMutableTreeNode item, final JsonNode node) {
		try{fillSettings(ns, node);
			
			if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
				node.getChild(F_TYPE).setValue(ns.type.toString());
				node.getChild(F_NAME).setValue(ns.name);
				node.getChild(F_CAPTION).setValue(ns.caption);
				((DefaultTreeModel)getModel()).nodeChanged(item);
			}
		} catch (LocalizationException exc) {
			logger.message(Severity.error, "Error showing settings: "+exc.getLocalizedMessage(), exc);
		}
	}

	private void insertFile(final File item, final DefaultMutableTreeNode toItem, final JsonNode toNode) {
		final ItemAndNode	sel = getSelection();
		
		if (sel != null) {
			final int	suffix = uniqueNameSuffix++;
			
			ns.type = ContentNodeType.LEAF;
			ns.name = item.getName()+suffix;
			ns.caption = item.getName()+suffix;
			
			try{if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
					ns.id = UUID.randomUUID().toString();
					
					insertChild(sel.item, sel.node, new JsonNode(JsonNodeType.JsonObject 
							, new JsonNode(ns.id).setName(F_ID)
							, new JsonNode(ns.type.name()).setName(F_TYPE)
							, new JsonNode(ns.name).setName(F_NAME)
							, new JsonNode(ns.caption).setName(F_CAPTION)
					));
				}
			} catch (LocalizationException e) {
			}
		}
	}	

	private ItemAndNode getSelection(final Point point) {
		final TreePath	path = getPathForLocation(point.x, point.x);
		
		if (path != null) {
			final DefaultMutableTreeNode	item = (DefaultMutableTreeNode)path.getLastPathComponent();
			final JsonNode					node = (JsonNode) item.getUserObject();
			
			return new ItemAndNode(item, node);
		}
		else {
			return null;
		}
	}

	private ItemAndNode getSelection() {
		final TreePath	path = getSelectionPath();
		
		if (path != null) {
			final DefaultMutableTreeNode	item = (DefaultMutableTreeNode)path.getLastPathComponent();
			final JsonNode					node = (JsonNode) item.getUserObject();
			
			return new ItemAndNode(item, node);
		}
		else {
			return null;
		}
	}
	
	private Point getRectCenter(final Rectangle bounds) {
		return new Point(bounds.x+bounds.width/2, bounds.y+bounds.height/2);
	}

	private static void fillSettings(final NodeSettings settings, final JsonNode node) {
		settings.type = ContentNodeType.valueOf(node.getChild(F_TYPE).getStringValue());
		settings.id = node.getChild(F_ID).getStringValue();
		settings.name = node.getChild(F_NAME).getStringValue();
		settings.caption = node.getChild(F_CAPTION).getStringValue();
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
	    	if (info.isDataFlavorSupported(FLAVORS[0]) || info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	    		return isNodeAvailable4Drop(info.getComponent(), info.getDropLocation().getDropPoint());
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
					final DefaultMutableTreeNode	toItem = (DefaultMutableTreeNode)path.getLastPathComponent();  
					final JsonNode					toNode = (JsonNode) (toItem).getUserObject();
		    		
		    		if (t.isDataFlavorSupported(FLAVORS[0])) {
						final JsonNode					fromNode = (JsonNode) t.getTransferData(FLAVORS[0]);
						
				        processDragAndDrop(action == MOVE, toItem, fromNode, toItem, toNode);
				        return true;
		    		}
		    		else {
		    			return false;
		    		}
				} catch (UnsupportedFlavorException | IOException e) {
					return false;
				}
	        }
	        else {
	    		try{final Point						point = info.getDropLocation().getDropPoint();
		    		final TreePath					path = ((JTree)info.getComponent()).getPathForLocation(point.x, point.y);
		    		final Transferable				t = info.getTransferable();
					final DefaultMutableTreeNode	toItem = (DefaultMutableTreeNode)path.getLastPathComponent();  
					final JsonNode					toNode = (JsonNode) (toItem).getUserObject();
					
		    		if (t.isDataFlavorSupported(FLAVORS[0])) {
						final JsonNode				fromNode = (JsonNode) t.getTransferData(FLAVORS[0]);
			        	
			        	processDragAndDrop(action == MOVE, toItem, fromNode, toItem, toNode);
			        	return true;
					}					
		    		else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		    			for (File item : (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor)) {
		    				insertFile(item, toItem, toNode);
		    			}
				        return true;
		    		}
					else {
						return false;
					}
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
	    
	    private boolean isNodeAvailable4Drop(final Component c, final Point point) {
	    	if (c instanceof JTree) {
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
	
	private static class ItemAndNode {
		private final DefaultMutableTreeNode	item;
		private final JsonNode					node;
		
		public ItemAndNode(DefaultMutableTreeNode item, JsonNode node) {
			this.item = item;
			this.node = node;
		}

		@Override
		public String toString() {
			return "ItemAndNode [item=" + item + ", node=" + node + "]";
		}
	}
}
