package chav1961.purelib.navigator;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.Format;

@LocaleResourceLocation("i18n:prop:chav1961/purelib/navigator/i18n/i18n")
@LocaleResource(value="NavigationFilter",tooltip="NavigationFilter.tt")	
public class NavigationFilter {
	@LocaleResource(value="NavigationFilter.showDeprecated",tooltip="NavigationFilter.showDeprecated.tt")
	@Format("")
	public boolean	showDeprecated = false;
	@LocaleResource(value="NavigationFilter.showBeta",tooltip="NavigationFilter.showBeta.tt")	
	@Format("")
	public boolean	showBeta = false;
	@LocaleResource(value="NavigationFilter.hideNonThreadSafe",tooltip="NavigationFilter.hideNonThreadSafe.tt")	
	@Format("")
	public boolean	hideNonThreadSafe = false;
	@LocaleResource(value="NavigationFilter.hideRestricted",tooltip="NavigationFilter.hideRestricted.tt")	
	@Format("")
	public boolean	hideRestricted = true;
}
