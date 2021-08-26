package chav1961.purelibnavigator.admin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
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
import java.util.Locale;
import java.util.Map;
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
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.FSM.FSMLine;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.ui.interfaces.UIItemState;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;
import chav1961.purelibnavigator.interfaces.ContentNodeType;

public class StaticTreeContent extends JTree implements LocaleChangeListener {
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

	private static final String				FILE_TYPE_CREOLE = "chav1961.purelibnavigator.admin.StaticTreeContent.fileType.creole";
	private static final String				FILE_TYPE_IMAGE = "chav1961.purelibnavigator.admin.StaticTreeContent.fileType.image";
	private static final String				REMOVE_LEAF_TITLE = "chav1961.purelibnavigator.admin.StaticTreeContent.removeLeaf.title";
	private static final String				REMOVE_LEAF_MESSAGE = "chav1961.purelibnavigator.admin.StaticTreeContent.removeLeaf.message";
	private static final String				REMOVE_SUBTREE_TITLE = "chav1961.purelibnavigator.admin.StaticTreeContent.removeSubtree.title";
	private static final String				REMOVE_SUBTREE_MESSAGE = "chav1961.purelibnavigator.admin.StaticTreeContent.removeSubtree.message";
	
	private static final DataFlavor[]		FLAVORS;

	private static enum FormEditTerminal {
		INSERT_SIBLING,
		INSERT_CHILDREN,
		INSERT_FILE,
		DUPLICATE_LEAF,
		SHOW_PROPERTIES,
		COMPLETE,
		CANCEL
	}
	
	private static enum FormEditState {
		ORDINAL(Utils.mkMap("type", UIItemState.AvailableAndVisible.DEFAULT, "name", UIItemState.AvailableAndVisible.DEFAULT, "caption", UIItemState.AvailableAndVisible.DEFAULT)),
		NEW_SUBTREE(Utils.mkMap("type", UIItemState.AvailableAndVisible.AVAILABLE, "name", UIItemState.AvailableAndVisible.AVAILABLE, "caption", UIItemState.AvailableAndVisible.AVAILABLE)),
		NEW_LEAF(Utils.mkMap("type", UIItemState.AvailableAndVisible.NOTAVAILABLE, "name", UIItemState.AvailableAndVisible.AVAILABLE, "caption", UIItemState.AvailableAndVisible.AVAILABLE)),
		DUPLICATE_LEAF(Utils.mkMap("type", UIItemState.AvailableAndVisible.NOTAVAILABLE, "name", UIItemState.AvailableAndVisible.AVAILABLE, "caption", UIItemState.AvailableAndVisible.AVAILABLE)),
		EDIT_LEAF(Utils.mkMap("type", UIItemState.AvailableAndVisible.NOTAVAILABLE, "name", UIItemState.AvailableAndVisible.AVAILABLE, "caption", UIItemState.AvailableAndVisible.AVAILABLE));
		
		private final Map<String,UIItemState.AvailableAndVisible> props;
		
		private FormEditState(final Map<String,UIItemState.AvailableAndVisible> props) {
			this.props = props;
		}
		
		public Map<String, UIItemState.AvailableAndVisible> getProps() {
			return props;
		}
	}

	private static enum SelectionTerminal {
		SELECT_SUBTREE,
		SELECT_LEAF,
		UNSELECT
	}

	private static enum SelectionState {
		ORDINAL,
		PROCESS_SUBTREE,
		PROCESS_LEAF;
	}
	
	@SuppressWarnings("unchecked")
	private static FSMLine<FormEditTerminal, FormEditState, FormEditState>[]	FSM_PROP = new FSMLine[]{
																					new FSMLine<>(FormEditState.ORDINAL, FormEditTerminal.INSERT_SIBLING, FormEditState.NEW_SUBTREE),
																					new FSMLine<>(FormEditState.NEW_SUBTREE, FormEditTerminal.COMPLETE, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.NEW_SUBTREE, FormEditTerminal.CANCEL, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.ORDINAL, FormEditTerminal.INSERT_CHILDREN, FormEditState.NEW_SUBTREE),
																					new FSMLine<>(FormEditState.NEW_SUBTREE, FormEditTerminal.COMPLETE, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.NEW_SUBTREE, FormEditTerminal.CANCEL, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.ORDINAL, FormEditTerminal.INSERT_FILE, FormEditState.NEW_LEAF),
																					new FSMLine<>(FormEditState.NEW_LEAF, FormEditTerminal.COMPLETE, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.NEW_LEAF, FormEditTerminal.CANCEL, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.ORDINAL, FormEditTerminal.DUPLICATE_LEAF, FormEditState.DUPLICATE_LEAF),
																					new FSMLine<>(FormEditState.DUPLICATE_LEAF, FormEditTerminal.COMPLETE, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.DUPLICATE_LEAF, FormEditTerminal.CANCEL, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.ORDINAL, FormEditTerminal.SHOW_PROPERTIES, FormEditState.EDIT_LEAF),
																					new FSMLine<>(FormEditState.EDIT_LEAF, FormEditTerminal.COMPLETE, FormEditState.ORDINAL),
																					new FSMLine<>(FormEditState.EDIT_LEAF, FormEditTerminal.CANCEL, FormEditState.ORDINAL),
																				};
	@SuppressWarnings("unchecked")
	private static FSMLine<SelectionTerminal, SelectionState, SelectionState>[]	FSM_MENU = new FSMLine[]{
																					new FSMLine<>(SelectionState.ORDINAL, SelectionTerminal.SELECT_SUBTREE, SelectionState.PROCESS_SUBTREE),
																					new FSMLine<>(SelectionState.ORDINAL, SelectionTerminal.SELECT_LEAF, SelectionState.PROCESS_LEAF),
																					new FSMLine<>(SelectionState.PROCESS_SUBTREE, SelectionTerminal.UNSELECT, SelectionState.ORDINAL),
																					new FSMLine<>(SelectionState.PROCESS_SUBTREE, SelectionTerminal.SELECT_LEAF, SelectionState.PROCESS_LEAF),
																					new FSMLine<>(SelectionState.PROCESS_LEAF, SelectionTerminal.UNSELECT, SelectionState.ORDINAL),
																					new FSMLine<>(SelectionState.PROCESS_LEAF, SelectionTerminal.SELECT_SUBTREE, SelectionState.PROCESS_SUBTREE),
																				};
	
	private FSM<FormEditTerminal, FormEditState, FormEditState, Object>			fsmProp = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{}, FormEditState.ORDINAL, FSM_PROP);
	private FSM<SelectionTerminal, SelectionState, SelectionState, Object>		fsmMenu = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{}, SelectionState.ORDINAL, FSM_MENU);
	
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
	private TimerTask						tt = null;
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
			this.nodeMenu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.rightNodeMenu")), JPopupMenu.class, (meta)->getAccessAndVisibility(meta));
			this.leafMenu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.rightLeafMenu")), JPopupMenu.class, (meta)->getAccessAndVisibility(meta));
			this.emptyMenu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.rightEmptyMenu")), JPopupMenu.class, (meta)->getAccessAndVisibility(meta));
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
				switch (fsmMenu.getCurrentState()) {
					case ORDINAL			:
						break;
					case PROCESS_LEAF		:
						leafRemove();
						break;
					case PROCESS_SUBTREE	:
						nodeRemoveSubtree();
						break;
					default : throw new UnsupportedOperationException("Current menu state ["+fsmMenu.getCurrentState()+"] is not supported yet"); 
				}
			}, SwingUtils.ACTION_DELETE);
			SwingUtils.assignActionKey(this, SwingUtils.KS_INSERT, (e)->{
				switch (fsmMenu.getCurrentState()) {
					case ORDINAL : case PROCESS_LEAF :
						leafRemove();
						break;
					case PROCESS_SUBTREE	:
						nodeInsertChild();
						break;
					default : throw new UnsupportedOperationException("Current menu state ["+fsmMenu.getCurrentState()+"] is not supported yet"); 
				}
			}, SwingUtils.ACTION_INSERT);
			SwingUtils.assignActionKey(this, SwingUtils.KS_DUPLICATE, (e)->{
				switch (fsmMenu.getCurrentState()) {
					case ORDINAL : case PROCESS_SUBTREE :
						break;
					case PROCESS_LEAF		:
						leafDuplicate();
						break;
					default : throw new UnsupportedOperationException("Current menu state ["+fsmMenu.getCurrentState()+"] is not supported yet"); 
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
							switch (fsmMenu.getCurrentState()) {
								case ORDINAL			:
									break;
								case PROCESS_LEAF : case PROCESS_SUBTREE	:
									if (e.getClickCount() >= 2) {
										showSettings(sel.item, sel.node);
									}
									break;
								default : throw new UnsupportedOperationException("Current menu state ["+fsmMenu.getCurrentState()+"] is not supported yet"); 
							}
							break;
						case MouseEvent.BUTTON2 :
							break;
						case MouseEvent.BUTTON3 :
							final int	row = getRowForLocation(e.getPoint().x, e.getPoint().y);
							
							if (row != getLeadSelectionRow()) {
								setSelectionRow(row);
							}
							SwingUtilities.invokeLater(()->{
								switch (fsmMenu.getCurrentState()) {
									case ORDINAL			:
										showPopup(getRectCenter(getVisibleRect()), emptyMenu);
										break;
									case PROCESS_LEAF		:
										showPopup(getRectCenter(getPathBounds(getSelectionPath())), leafMenu);
										break;
									case PROCESS_SUBTREE	:
										showPopup(getRectCenter(getPathBounds(getSelectionPath())), nodeMenu);
										break;
									default : throw new UnsupportedOperationException("Current menu state ["+fsmMenu.getCurrentState()+"] is not supported yet"); 
								}
							});
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
							switch (fsmMenu.getCurrentState()) {
								case ORDINAL			:
									showPopup(getRectCenter(getVisibleRect()), emptyMenu);
									break;
								case PROCESS_LEAF		:
									showPopup(getRectCenter(getPathBounds(getSelectionPath())), leafMenu);
									break;
								case PROCESS_SUBTREE	:
									showPopup(getRectCenter(getPathBounds(getSelectionPath())), nodeMenu);
									break;
								default : throw new UnsupportedOperationException("Current menu state ["+fsmMenu.getCurrentState()+"] is not supported yet"); 
							}
							break;
						case KeyEvent.VK_ENTER :
							switch (fsmMenu.getCurrentState()) {
								case ORDINAL	:
									break;
								case PROCESS_LEAF : case PROCESS_SUBTREE	:
									final ItemAndNode	selEnter = getSelection();
									
									showSettings(selEnter.item, selEnter.node);
									break;
								default : throw new UnsupportedOperationException("Current menu state ["+fsmMenu.getCurrentState()+"] is not supported yet"); 
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
				
				try{final ItemAndNode	sel = getSelection();

					if (sel == null) {
						fsmMenu.processTerminal(SelectionTerminal.UNSELECT, null);
					}
					else if (sel.node.hasName(F_CONTENT)) {
						fsmMenu.processTerminal(SelectionTerminal.SELECT_SUBTREE, null);
					}
					else {
						fsmMenu.processTerminal(SelectionTerminal.SELECT_LEAF, null);
					}
				} catch (FlowException exc) {
					printError(exc);
				}
			});

			this.ns = new NodeSettings(logger);
			this.form = new AutoBuiltForm<NodeSettings>(ContentModelFactory.forAnnotatedClass(NodeSettings.class), localizer, PureLibSettings.INTERNAL_LOADER, ns, ns, (meta)->getAccessAndVisibility(meta));
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
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(nodeMenu, oldLocale, newLocale);
		SwingUtils.refreshLocale(leafMenu, oldLocale, newLocale);
		SwingUtils.refreshLocale(emptyMenu, oldLocale, newLocale);
		SwingUtils.refreshLocale(form, oldLocale, newLocale);
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

	@OnAction("action:/nodePasteFile")
	private void nodePasteFile() {
		final ItemAndNode	sel = getSelection();
		
		if (sel != null) {
			try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(URI.create("fsys:file:./"))) {
				
				for (String item : JFileSelectionDialog.select((JFrame)null, localizer, logger, fsi,
										JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS, 
										FilterCallback.of(localizer.getValue(FILE_TYPE_CREOLE), "*.cre"), FilterCallback.of(localizer.getValue(FILE_TYPE_IMAGE), "*.png"))) {
					insertFile(new File(item), sel.item, sel.node);
				}
			} catch (LocalizationException | IOException exc) {
				printError(exc);
			}
		}
	}
	
	@OnAction("action:/nodeInsertSibling")
	private void nodeInsertSibling() {
		final ItemAndNode	sel = getSelection();
		
		if (sel != null) {
			final int	suffix = uniqueNameSuffix++;
			
			ns.type = ContentNodeType.valueOf(sel.node.getChild(F_TYPE).getStringValue());
			ns.name = sel.node.getChild(F_NAME).getStringValue()+suffix;
			ns.caption = sel.node.getChild(F_CAPTION).getStringValue()+suffix;
			
			try{fsmProp.processTerminal(FormEditTerminal.INSERT_SIBLING, null);
			
				if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
					fsmProp.processTerminal(FormEditTerminal.COMPLETE, null);
					
					ns.id = UUID.randomUUID().toString();
					insertSibling(sel.item, sel.node, new JsonNode(JsonNodeType.JsonObject 
							, new JsonNode(ns.id).setName(F_ID)
							, new JsonNode(ns.type.name()).setName(F_TYPE)
							, new JsonNode(ns.name).setName(F_NAME)
							, new JsonNode(ns.caption).setName(F_CAPTION)
					));
				}
				else {
					fsmProp.processTerminal(FormEditTerminal.CANCEL, null);
				}
			} catch (LocalizationException | FlowException exc) {
				printError(exc);
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
			
			try{fsmProp.processTerminal(FormEditTerminal.INSERT_SIBLING, null);
				
				if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
					fsmProp.processTerminal(FormEditTerminal.COMPLETE, null);
					
					ns.id = UUID.randomUUID().toString();
					insertChild(sel.item, sel.node, new JsonNode(JsonNodeType.JsonObject 
							, new JsonNode(ns.id).setName(F_ID)
							, new JsonNode(ns.type.name()).setName(F_TYPE)
							, new JsonNode(ns.name).setName(F_NAME)
							, new JsonNode(ns.caption).setName(F_CAPTION)
					));
				}
				else {
					fsmProp.processTerminal(FormEditTerminal.CANCEL, null);
				}
			} catch (LocalizationException | FlowException exc) {
				printError(exc);
			}
		}
	}

	@OnAction("action:/nodeRemoveSubtree")
	private void nodeRemoveSubtree() {
		try{final ItemAndNode	sel = getSelection();
			
			if (sel != null && new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(REMOVE_SUBTREE_MESSAGE, sel.node.getChild(F_NAME).getStringValue()), REMOVE_SUBTREE_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				removeItem(sel.item, sel.node);
			}
		} catch (LocalizationException exc) {
			printError(exc);
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
				
				fsmProp.processTerminal(FormEditTerminal.DUPLICATE_LEAF, null);

				if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
					fsmProp.processTerminal(FormEditTerminal.COMPLETE, null);
					
					ns.id = UUID.randomUUID().toString();					
					final JsonNode	newNode = new JsonNode(JsonNodeType.JsonObject 
														, new JsonNode(UUID.randomUUID().toString()).setName(F_ID)
														, new JsonNode(ContentNodeType.LEAF.toString()).setName(F_TYPE)
														, new JsonNode(ns.name).setName(F_NAME)
														, new JsonNode(ns.caption).setName(F_CAPTION)
														);
					insertChild((DefaultMutableTreeNode)sel.item.getParent(),(JsonNode)((DefaultMutableTreeNode)sel.item.getParent()).getUserObject(),newNode);
				}
				else {
					fsmProp.processTerminal(FormEditTerminal.CANCEL, null);
				}
			} catch (LocalizationException | FlowException exc) {
				printError(exc);
			}
		}
	}

	@OnAction("action:/leafRemove")
	private void leafRemove() {
		try{final ItemAndNode	sel = getSelection();
			
			if (sel != null && new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(REMOVE_LEAF_MESSAGE, sel.node.getChild(F_NAME).getStringValue()), REMOVE_LEAF_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				removeItem(sel.item, sel.node);
			}
		} catch (LocalizationException exc) {
			printError(exc);
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
			fsmProp.processTerminal(FormEditTerminal.SHOW_PROPERTIES, null);
			
			if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
				fsmProp.processTerminal(FormEditTerminal.COMPLETE, null);
				
				node.getChild(F_TYPE).setValue(ns.type.toString());
				node.getChild(F_NAME).setValue(ns.name);
				node.getChild(F_CAPTION).setValue(ns.caption);
				((DefaultTreeModel)getModel()).nodeChanged(item);
			}
			else {
				fsmProp.processTerminal(FormEditTerminal.CANCEL, null);
			}
		} catch (LocalizationException | FlowException exc) {
			printError(exc);
		}
	}

	private void insertFile(final File item, final DefaultMutableTreeNode toItem, final JsonNode toNode) {
		final ItemAndNode	sel = getSelection();
		
		if (sel != null) {
			final int	suffix = uniqueNameSuffix++;
			
			ns.type = ContentNodeType.LEAF;
			ns.name = item.getName()+suffix;
			ns.caption = item.getName()+suffix;
			
			try{fsmProp.processTerminal(FormEditTerminal.INSERT_FILE, null);
				
				if (AutoBuiltForm.ask((JFrame)null, localizer, form)) {
					fsmProp.processTerminal(FormEditTerminal.COMPLETE, null);
					ns.id = UUID.randomUUID().toString();
					
					insertChild(sel.item, sel.node, new JsonNode(JsonNodeType.JsonObject 
							, new JsonNode(ns.id).setName(F_ID)
							, new JsonNode(ns.type.name()).setName(F_TYPE)
							, new JsonNode(ns.name).setName(F_NAME)
							, new JsonNode(ns.caption).setName(F_CAPTION)
					));
				}
				else {
					fsmProp.processTerminal(FormEditTerminal.CANCEL, null);
				}
			} catch (LocalizationException | FlowException exc) {
				printError(exc);
			}
		}
	}	

	private ItemAndNode getSelection(final Point point) {
		final int 		row = getRowForLocation(point.x, point.y);
		
		if (row >= 0) {
			final TreePath	path = getPathForRow(row);
			
			if (path != null) {
				final DefaultMutableTreeNode	item = (DefaultMutableTreeNode)path.getLastPathComponent();
				final JsonNode					node = (JsonNode) item.getUserObject();
				
				return new ItemAndNode(item, node);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	private ItemAndNode getSelection() {
		final TreePath	path = getSelectionModel().getSelectionPath();
		
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

	
	private void printError(final Throwable exc) {
		logger.message(Severity.error, exc.getLocalizedMessage(), exc);
	}

	private UIItemState.AvailableAndVisible getAccessAndVisibility(final ContentNodeMetadata meta) {
//		System.err.println("Name: "+meta.getApplicationPath()+", "+meta.getName());
		
		final Map<String,UIItemState.AvailableAndVisible>	aav = fsmProp.getCurrentState().getProps();
		
		if (aav.containsKey(meta.getName())) {
			return aav.get(meta.getName());
		}
		else if (URI.create("app:action:/leafPaste").equals(meta.getApplicationPath()) || URI.create("app:action:/nodePaste").equals(meta.getApplicationPath())) {
			return Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(FLAVORS[0]) ? UIItemState.AvailableAndVisible.AVAILABLE : UIItemState.AvailableAndVisible.NOTAVAILABLE;  
		}
		else {
			return UIItemState.AvailableAndVisible.DEFAULT;
		}
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
