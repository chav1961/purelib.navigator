package chav1961.purelibnavigator.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelibnavigator.interfaces.ContentNodeType/chav1961/purelib/admin/i18n/i18n.xml")
public enum ContentNodeType {
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.leaf",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.leaf.tt")
	LEAF,
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.subtree",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.subtree.tt")
	SUBTREE,
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.contentNodeType.unknown",tooltip="chav1961.purelibnavigator.interfaces.contentNodeType.unknown.tt")
	UNKNOWN
}
