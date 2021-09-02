package chav1961.purelibnavigator.admin;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.cdb.CompilerUtils;
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
import chav1961.purelibnavigator.interfaces.TreeSelectionCallback;

public class ResourceTreeContent extends JTree implements LocaleChangeListener {
	private static final long serialVersionUID = 1L;

	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final TreeSelectionCallback		callback;
	private FileSystemInterface				fsi =  null;
	
	public ResourceTreeContent(final ContentMetadataInterface mdi, final Localizer localizer, final LoggerFacade logger, final TreeSelectionCallback callback) throws ContentException, LocalizationException {
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
							label.setText(node.getStringValue());
						case JsonReal: case JsonBoolean: case JsonInteger: case JsonNull:
							break;
						default :
							throw new UnsupportedOperationException("Json node type ["+node.getType()+"] is not supported yet");
					}
					return label;
				}
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
