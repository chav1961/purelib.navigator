package chav1961.purelibnavigator.admin.entities;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelibnavigator.interfaces.ContentNodeType;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelibnavigator.admin.entities.NodeSettings/chav1961/purelib/admin/i18n/i18n.xml")
@LocaleResource(value="chav1961.purelibnavigator.admin.nodesettings",tooltip="chav1961.purelibnavigator.admin.nodesettings.tt",help="help.aboutApplication")
public class NodeSettings implements FormManager<Object,NodeSettings>, ModuleAccessor {
	@LocaleResource(value="chav1961.purelibnavigator.admin.nodesettings.type",tooltip="chav1961.purelibnavigator.admin.nodesettings.type.tt")
	@Format("30ms")
	public ContentNodeType	type = ContentNodeType.UNKNOWN;
	
	@LocaleResource(value="chav1961.purelibnavigator.admin.nodesettings.caption",tooltip="chav1961.purelibnavigator.admin.nodesettings.caption.tt")
	@Format("30ms")
	public String			caption = "";

	public String			id = "";
	
	
	public NodeSettings() {
	}
	
	@Override
	public RefreshMode onField(final LoggerFacade logger, final NodeSettings inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		// TODO Auto-generated method stub
		return RefreshMode.DEFAULT;
	}
	
	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	@Override
	public String toString() {
		return "NodeSettings [type=" + type + ", caption=" + caption + ", id=" + id + "]";
	}
}
