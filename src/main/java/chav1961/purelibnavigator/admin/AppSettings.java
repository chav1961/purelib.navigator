package chav1961.purelibnavigator.admin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelibnavigator.interfaces.PackingType;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelibnavigator.admin.AppSettings/chav1961/purelib/admin/i18n/i18n.xml")
@LocaleResource(value="chav1961.purelibnavigator.admin.appsettings",tooltip="chav1961.purelibnavigator.admin.appsettings.tt",help="help.aboutApplication")
public class AppSettings implements FormManager<Object,AppSettings>, ModuleAccessor {
	private static final String		PACKING_TYPE_PROP = "packingType";
	private static final String		INCLUDE_JAVADOC_PROP = "includeJavaDoc";
	private static final String		JAVADOC_LOCATION_PROP = "javaDocLocation";
	
	@LocaleResource(value="chav1961.purelibnavigator.admin.appsettings.type",tooltip="chav1961.purelibnavigator.admin.appsettings.type.tt")
	@Format("30ms")
	public PackingType	type = PackingType.WAR;
	
	@LocaleResource(value="chav1961.purelibnavigator.admin.appsettings.includejavadoc",tooltip="chav1961.purelibnavigator.admin.appsettings.includejavadoc.tt")
	@Format("30ms")
	public boolean		includeJavaDoc = false;

	@LocaleResource(value="chav1961.purelibnavigator.admin.appsettings.javadoclocation",tooltip="chav1961.purelibnavigator.admin.appsettings.javadoclocation.tt")
	@Format("30ms")
	public File			javaDocLocation = new File("./");
	
	private final LoggerFacade 	logger;

	public AppSettings(final LoggerFacade logger) {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			this.logger = logger;
		}
	}

	@Override
	public RefreshMode onField(final AppSettings inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
	
	public void load(final InputStream is) throws IOException{
		if (is == null) {
			throw new NullPointerException("Input stream can't be null"); 
		}
		else {
			final SubstitutableProperties	props = new SubstitutableProperties();
			
			props.load(is);
			type = props.getProperty(PACKING_TYPE_PROP, PackingType.class, PackingType.WAR.name());
			includeJavaDoc = props.getProperty(INCLUDE_JAVADOC_PROP, boolean.class, "false");
			javaDocLocation = props.getProperty(JAVADOC_LOCATION_PROP, File.class, "./");
		}
	}
	
	public void save(final OutputStream os) throws IOException{
		if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else {
			final SubstitutableProperties	props = new SubstitutableProperties();
			
			props.setProperty(PACKING_TYPE_PROP, type.name());
			props.setProperty(INCLUDE_JAVADOC_PROP, String.valueOf(includeJavaDoc));
			props.setProperty(JAVADOC_LOCATION_PROP, javaDocLocation.getAbsolutePath());
			props.store(os, null);
		}
	}
}
