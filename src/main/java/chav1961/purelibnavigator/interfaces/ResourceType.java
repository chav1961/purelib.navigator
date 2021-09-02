package chav1961.purelibnavigator.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public enum ResourceType {
	NONE,
	CREOLE(".cre", ResourceType.class.getResource("default.cre")),
	IMAGE(".png", ResourceType.class.getResource("default.png")),
	RESOURCE(".*", ResourceType.class.getResource("default.txt"));
	
	private final boolean	hasResource;
	private final String 	resourceSuffix;
	private final URL		defaultResource;

	private ResourceType() {
		this.hasResource = false;
		this.resourceSuffix = null;
		this.defaultResource = null;
	}
	
	private ResourceType(final String resourceSuffix, final URL defaultResource) {
		this.hasResource = true;
		this.resourceSuffix = resourceSuffix;
		this.defaultResource = defaultResource;
	}

	public boolean hasResource() {
		return hasResource;
	}
	
	public String getResourceSuffix() {
		return resourceSuffix;
	}
	
	public InputStream getDefaultResource() throws IOException {
		return defaultResource.openStream();
	}
}
