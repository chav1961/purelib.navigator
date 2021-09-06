package chav1961.purelibnavigator.admin;


import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.concurrent.OptionalTimerTask;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelibnavigator.admin.entities.TreeContentNode;
import chav1961.purelibnavigator.interfaces.ContentNodeGroup;
import chav1961.purelibnavigator.interfaces.ContentNodeType;
import chav1961.purelibnavigator.interfaces.ResourceType;
import chav1961.purelibnavigator.interfaces.TreeManipulationCallback;

class ResourceTreeContent extends JTree implements LocaleChangeListener {
	private static final long serialVersionUID = 1L;
	private static final long				TT_DELAY = 500;

	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final TreeManipulationCallback	callback;
	private OptionalTimerTask				tt = null;
	private FileSystemInterface				fsi =  null;
	
	public ResourceTreeContent(final ContentMetadataInterface mdi, final Localizer localizer, final LoggerFacade logger, final TreeManipulationCallback callback) throws ContentException, LocalizationException {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Tree selection callback can't be null"); 
		}
		else {
			this.mdi = mdi;
			this.localizer = localizer;
			this.logger = logger;
			this.callback = callback;

			((DefaultTreeModel)getModel()).setRoot(new TreeContentNode(new JsonNode(JsonNodeType.JsonObject, new JsonNode("undefined").setName(AdminUtils.F_CAPTION), new JsonNode("SUBTREE").setName(AdminUtils.F_TYPE))));
			setRootVisible(true);
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			setCellRenderer(new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					final JLabel	label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					final JsonNode	node = ((TreeContentNode)value).getUserObject(); 

					switch (node.getType()) {
						case JsonArray:
							label.setText("[...]");
							break;
						case JsonObject:
							final ContentNodeType	type = ContentNodeType.valueOf(node.getChild(AdminUtils.F_TYPE).getStringValue());
							
							try{label.setText(localizer.getValue(CompilerUtils.extractAnnotation(type, LocaleResource.class).value()));
							} catch (LocalizationException e) {
								label.setText(type.name());
							}
							break;
						case JsonString:
							final ContentNodeType	cnt = ContentNodeType.byFileNameSuffix(node.getStringValue());
							
							label.setText(node.getStringValue());
							label.setIcon(cnt.getResourceType().getIcon());
						case JsonReal: case JsonBoolean: case JsonInteger: case JsonNull:
							break;
						default :
							throw new UnsupportedOperationException("Json node type ["+node.getType()+"] is not supported yet");
					}
					return label;
				}
			});
			getSelectionModel().addTreeSelectionListener((e)->{
				if (tt != null) {
					tt.reject();
				}
				tt = new OptionalTimerTask(()->{
						final ItemAndNode	sel = getSelection();
						
						if (sel != null) {
							callback.select(sel.item, sel.node);
						}
						else {
							callback.select(null, null);
						}
					}
				);
				PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, TT_DELAY);
			});
			
		}
	}	
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
	}

	public void setFileSystem(final FileSystemInterface fsi, final JsonNode resources) throws ContentException {
		if (fsi == null) {
			throw new NullPointerException("File system can't be null"); 
		}
		else {
			this.fsi = fsi;
			((DefaultTreeModel)getModel()).setRoot(buildContentTree(resources));
		}
	}
	
	public void addResource(final JsonNode resource) {
		if (resource == null) {
			throw new NullPointerException("Resource to add can't be null"); 
		}
		else {
			final ResourceType	type = ContentNodeType.valueOf(resource.getChild(AdminUtils.F_TYPE).getStringValue()).getResourceType();
			
			switch (type) {
				case CREOLE : case IMAGE : case RESOURCE :
					final ItemAndNode	ian = seekNode((TreeContentNode)((DefaultTreeModel)getModel()).getRoot(),type); 
					
					insertChild(ian, resource, type);
					((DefaultTreeModel)getModel()).nodeStructureChanged(ian.item);
					break;
				case NONE	:
					break;
				default :
					throw new UnsupportedOperationException("Resource type ["+type+"] is not supported yet");
			}
		}
	}
	
	public void removeResource(final JsonNode resource) {
		if (resource == null) {
			throw new NullPointerException("Resource to remove can't be null"); 
		}
		else {
			final ResourceType	type = ContentNodeType.valueOf(resource.getChild(AdminUtils.F_TYPE).getStringValue()).getResourceType();
			
			switch (type) {
				case CREOLE : case IMAGE : case RESOURCE :
					final ItemAndNode	ian = seekNode((TreeContentNode)((DefaultTreeModel)getModel()).getRoot(),type);
					
					removeChild(ian, resource);
					((DefaultTreeModel)getModel()).nodeStructureChanged(ian.item);
					break;
				case NONE	:
					break;
				default :
					throw new UnsupportedOperationException("Resource type ["+type+"] is not supported yet");
			}
		}
	}
	
	private ItemAndNode seekNode(final TreeContentNode root, final ResourceType type) {
		final JsonNode	rootNode = root.getUserObject(); 
		
		if (rootNode.getType() == JsonNodeType.JsonArray) {
			for (int index = 0; index < root.getChildCount(); index++) {
				final TreeContentNode	childItem = (TreeContentNode)root.getChildAt(index);
				final JsonNode			childNode = childItem.getUserObject();
				
				if (childNode.hasName(AdminUtils.F_TYPE) && ContentNodeType.valueOf(childNode.getChild(AdminUtils.F_TYPE).getStringValue()).getResourceType() == type) {
					return new ItemAndNode(childItem, childNode);
				}
			}
			throw new IllegalArgumentException("Unsupported resource type ["+type+"]");
		}
		else {
			throw new IllegalArgumentException("Resource tree structure corrupted");
		}
	}

	private void insertChild(final ItemAndNode parent, final JsonNode child, final ResourceType type) {
		final JsonNode			childNode = new JsonNode(child.getChild(AdminUtils.F_ID).getStringValue()+type.getResourceSuffix());
		final TreeContentNode	childItem = new TreeContentNode(childNode);
		
		parent.node.getChild(AdminUtils.F_CONTENT).addChild(childNode);
		parent.item.add(childItem);
	}

	private void removeChild(final ItemAndNode parent, final JsonNode child) {
		for (int index = 0; index < parent.item.getChildCount(); index++) {
			final TreeContentNode	childItem = (TreeContentNode)parent.item.getChildAt(index);
			final JsonNode			childNode = childItem.getUserObject();
			
			if (childNode.getStringValue().startsWith(child.getChild(AdminUtils.F_ID).getStringValue())) {
				parent.node.removeChild(index);
				parent.item.remove(index);
				break;
			}
		}
	}

	private ItemAndNode getSelection() {
		final TreePath	path = getSelectionModel().getSelectionPath();
		
		if (path != null) {
			final TreeContentNode	item = (TreeContentNode)path.getLastPathComponent();
			final JsonNode		node = item.getUserObject();
			
			return new ItemAndNode(item, node);
		}
		else {
			return null;
		}
	}
	
	static DefaultMutableTreeNode buildContentTree(final JsonNode node) throws ContentException {
		final List<JsonNode>	path = new ArrayList<>();
		final StringBuilder 	sb = new StringBuilder();
		
		if (node.getType() == JsonNodeType.JsonArray) {
			final TreeContentNode	root = new TreeContentNode(node, ContentNodeGroup.SUBTREE);
			
			path.add(node);
			
			for (JsonNode item : node.children()) {
				if (item.getType() == JsonNodeType.JsonObject) {
					path.add(item);
					
					if (JsonUtils.checkJsonMandatories(item, sb, AdminUtils.F_TYPE, AdminUtils.F_CONTENT)) {
						if (JsonUtils.checkJsonFieldTypes(item, sb, AdminUtils.F_TYPE+"/"+JsonUtils.JSON_TYPE_STR, AdminUtils.F_CONTENT+"/"+JsonUtils.JSON_TYPE_ARR)) {
							final TreeContentNode	type = new TreeContentNode(item, ContentNodeGroup.SUBTREE);

							for (JsonNode id : item.getChild(AdminUtils.F_CONTENT).children()) {
								type.add(new TreeContentNode(id));
							}
							root.add(type);
						}
						else {
							throw new ContentException("Illegal field(s) types ["+sb+"] at "+JsonUtils.printJsonPath(path));
						}
					}
					else {
						throw new ContentException("Mandatory field(s) ["+sb+"] is/are missing at "+JsonUtils.printJsonPath(path));
					}
					
					path.remove(path.size()-1);
				}
			}
			path.remove(path.size()-1);
			
			return root;
		}		
		else {
			throw new ContentException("Illegal JSON content format at \"resources\" clause: "+JsonUtils.printJsonPath(path));
		}
	}
}
