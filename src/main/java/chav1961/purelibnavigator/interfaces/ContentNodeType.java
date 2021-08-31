package chav1961.purelibnavigator.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelibnavigator.interfaces.ContentNodeType/chav1961/purelib/admin/i18n/i18n.xml")
public enum ContentNodeType {
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.creole",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.creole.tt")
	CREOLE(ContentNodeGroup.LEAF,".cre"),
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.image",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.image.tt")
	IMAGE(ContentNodeGroup.LEAF,".png"),
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.subtree",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.subtree.tt")
	SUBTREE(ContentNodeGroup.SUBTREE,""),
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.unknown",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.unknown.tt")
	UNKNOWN(ContentNodeGroup.SUBTREE,"");
	
	private final ContentNodeGroup	group;
	private final String			fileNameSuffix;
	
	private ContentNodeType(final ContentNodeGroup group, final String fileNameSuffix) {
		this.group = group;
		this.fileNameSuffix = fileNameSuffix;
	}
	
	public ContentNodeGroup getGroup() {
		return group;
	}
	
	public String getFileNameSuffix() {
		return fileNameSuffix;
	}
	
	public static ContentNodeType byFileNameSuffix(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty");
		}
		else if (fileName.endsWith(CREOLE.getFileNameSuffix())) {
			return CREOLE;
		}
		else if (fileName.endsWith(IMAGE.getFileNameSuffix())) {
			return IMAGE;
		}
		else {
			return UNKNOWN;
		}
	}
}
