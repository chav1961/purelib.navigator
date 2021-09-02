package chav1961.purelibnavigator.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelibnavigator.interfaces.ContentNodeType/chav1961/purelib/admin/i18n/i18n.xml")
public enum ContentNodeType {
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.creole",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.creole.tt")
	CREOLE(ContentNodeGroup.LEAF, ResourceType.CREOLE),
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.image",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.image.tt")
	IMAGE(ContentNodeGroup.LEAF, ResourceType.IMAGE),
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.resource",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.resource.tt")
	RESOURCE(ContentNodeGroup.LEAF, ResourceType.RESOURCE),
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.subtree",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.subtree.tt")
	SUBTREE(ContentNodeGroup.SUBTREE, ResourceType.NONE),
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.subtreewithcreole",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.subtreewithcreole.tt")
	SUBTREE_WITH_CREOLE(ContentNodeGroup.SUBTREE, ResourceType.CREOLE),
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.unknown",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.unknown.tt")
	UNKNOWN(ContentNodeGroup.SUBTREE, ResourceType. NONE);
	
	private final ContentNodeGroup	group;
	private final ResourceType		resourceType;
	
	private ContentNodeType(final ContentNodeGroup group, final ResourceType resourceType) {
		this.group = group;
		this.resourceType = resourceType;
	}
	
	public ContentNodeGroup getGroup() {
		return group;
	}
	
	public ResourceType getResourceType() {
		return resourceType;
	}
	
	public static ContentNodeType byFileNameSuffix(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty");
		}
		else if (fileName.endsWith(CREOLE.getResourceType().getResourceSuffix())) {
			return CREOLE;
		}
		else if (fileName.endsWith(IMAGE.getResourceType().getResourceSuffix())) {
			return IMAGE;
		}
		else if (fileName.endsWith(RESOURCE.getResourceType().getResourceSuffix())) {
			return RESOURCE;
		}
		else {
			return UNKNOWN;
		}
	}
}
