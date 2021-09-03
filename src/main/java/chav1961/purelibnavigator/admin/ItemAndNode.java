package chav1961.purelibnavigator.admin;

import chav1961.purelib.json.JsonNode;
import chav1961.purelibnavigator.admin.entities.TreeContentNode;

class ItemAndNode {
	final TreeContentNode	item;
	final JsonNode			node;
	
	ItemAndNode(final TreeContentNode item, final JsonNode node) {
		this.item = item;
		this.node = node;
	}

	@Override
	public String toString() {
		return "ItemAndNode [item=" + item + ", node=" + node + "]";
	}
}