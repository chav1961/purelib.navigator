package chav1961.purelibnavigator.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public enum ResourceType {
	NONE,
	CREOLE(".cre", "creole.png", "default.cre"),
	IMAGE(".png", "image.png", "default.png"),
	RESOURCE(".*", "resource.png", "default.txt");
	
	private final boolean	hasResource;
	private final String 	resourceSuffix;
	private final Icon		icon;
	private final URL		defaultResource;

	private ResourceType() {
		this.hasResource = false;
		this.resourceSuffix = null;
		this.icon = null;
		this.defaultResource = null;
	}
	
	private ResourceType(final String resourceSuffix, final String iconResource, final String defaultResource) {
		this.hasResource = true;
		this.resourceSuffix = resourceSuffix;
		this.icon = new ImageIcon(this.getClass().getResource(iconResource));
		this.defaultResource = this.getClass().getResource(defaultResource);
	}

	public boolean hasResource() {
		return hasResource;
	}
	
	public String getResourceSuffix() {
		return resourceSuffix;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public InputStream getDefaultResource() throws IOException {
		return defaultResource.openStream();
	}
}
