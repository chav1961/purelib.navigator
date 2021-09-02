package chav1961.purelibnavigator.admin.entities;

import javax.swing.tree.DefaultMutableTreeNode;

import chav1961.purelib.json.JsonNode;
import chav1961.purelibnavigator.admin.AdminUtils;
import chav1961.purelibnavigator.interfaces.ContentNodeGroup;
import chav1961.purelibnavigator.interfaces.ContentNodeType;

public class TreeContentNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;

	private final ContentNodeGroup	group;
	
	public TreeContentNode(final JsonNode node) {
		super(node);
		this.group = ContentNodeGroup.UNKNOWN;
	}

	public TreeContentNode(final JsonNode node, final ContentNodeGroup group) {
		super(node);
		this.group = group;
	}
	
	@Override
	public JsonNode getUserObject() {
		return (JsonNode)super.getUserObject();
	}
	
	@Override
	public boolean isLeaf() {
		switch (getUserObject().getType()) {
			case JsonArray	:
				return false;
			case JsonObject	:
				final ContentNodeType	type = ContentNodeType.valueOf(getUserObject().getChild(AdminUtils.F_TYPE).getStringValue());
				
				return group == ContentNodeGroup.UNKNOWN && type.getGroup() != ContentNodeGroup.SUBTREE || group == ContentNodeGroup.LEAF;
			case JsonBoolean : case JsonInteger : case JsonNull : case JsonReal : case JsonString :
				return true;
			default :
				throw new UnsupportedOperationException("Json node type ["+getUserObject().getType()+"] is not supported yet");
		}
	}
	
	@Override
	public TreeContentNode getParent() {
		return (TreeContentNode)super.getParent();
	}
}