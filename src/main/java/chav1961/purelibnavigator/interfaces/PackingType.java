package chav1961.purelibnavigator.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelibnavigator.interfaces.PackingType/chav1961/purelib/admin/i18n/i18n.xml")
public enum PackingType {
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.packingtype.war",tooltip="chav1961.purelibnavigator.interfaces.packingtype.war.tt")
	WAR,
	@LocaleResource(value="chav1961.purelibnavigator.interfaces.packingtype.standalone",tooltip="chav1961.purelibnavigator.interfaces.packingtype.standalone.tt")
	STANDALONE
}
