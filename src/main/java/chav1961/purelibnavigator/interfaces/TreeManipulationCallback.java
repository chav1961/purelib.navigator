package chav1961.purelibnavigator.interfaces;


import chav1961.purelib.json.JsonNode;
import chav1961.purelibnavigator.admin.entities.TreeContentNode;

@FunctionalInterface
public interface TreeManipulationCallback {
	void select(TreeContentNode item, JsonNode node);
	default void insert(TreeContentNode parentItem, JsonNode parentNode, TreeContentNode item, JsonNode node) {}
	default void change(TreeContentNode item, JsonNode node) {}
	default void delete(TreeContentNode parentItem, JsonNode parentNode, TreeContentNode item, JsonNode node) {}
}