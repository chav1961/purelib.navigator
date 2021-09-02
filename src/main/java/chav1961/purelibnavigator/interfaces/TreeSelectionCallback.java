package chav1961.purelibnavigator.interfaces;

import javax.swing.tree.DefaultMutableTreeNode;

import chav1961.purelib.json.JsonNode;

@FunctionalInterface
public interface TreeSelectionCallback {
	void process(DefaultMutableTreeNode item, JsonNode node);
}