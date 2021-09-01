package chav1961.purelibnavigator.admin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipOutputStream;

import javax.swing.tree.DefaultTreeModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelibnavigator.interfaces.ContentNodeGroup;
import chav1961.purelibnavigator.interfaces.ContentNodeType;
import chav1961.purelibnavigator.navigator.Navigator;
import chav1961.purelibnavigator.navigator.NavigatorHandler;

public class AdminUtils {
	public static final String			CONTENT_FILE = "content.json";

	public static final String			F_ID = "id";
	public static final String			F_TYPE = "type";
	public static final String			F_NAME = "name";
	public static final String			F_CAPTION = "caption";
	public static final String			F_CONTENT = "content";

	
	private static final InputStream	NULL_CONTENT = new InputStream() {@Override public int read() throws IOException {return -1;}}; 
	
	public static JsonNode loadContentDescriptor(final FileSystemInterface fsi) throws IOException, ContentException, NullPointerException {
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else {
			try(final FileSystemInterface		content = fsi.clone().open("/"+CONTENT_FILE)) {
				
				if (content.exists() && content.isFile()) {
					try(final Reader			rdr = content.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING);
						final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
						
						parser.next();
						return JsonUtils.loadJsonTree(parser);
					}
				}
				else {
					throw new ContentException("File system ["+fsi.getAbsoluteURI()+"] doesn't contain mandatory file ["+CONTENT_FILE+"] at the root"); 
				}
			}
		}
	}
	
	public static void packProject(final FileSystemInterface fsi, final OutputStream os, final AppSettings settings) throws IOException, NullPointerException {
		// TODO Auto-generated method stub
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else if (settings == null) {
			throw new NullPointerException("Packing settings can't be null"); 
		}
		else {
			try {
				switch (settings.type) {
					case STANDALONE	:
						packStandaloneProject(fsi, os, settings.includeJavaDoc ? settings.javaDocLocation : null);
						break;
					case WAR		:
						packWarProject(fsi, os, settings.includeJavaDoc ? settings.javaDocLocation : null);
						break;
					default : throw new UnsupportedOperationException("Packing type ["+settings.type+"] is not supported yet");
				}
			} catch (ContentException | URISyntaxException e) {
				throw new IOException(e);
			}
		}		
	}

	private static void packStandaloneProject(final FileSystemInterface fsi, final OutputStream os, final File javaDocLocation) throws IOException, ContentException, NullPointerException, URISyntaxException {
		// TODO Auto-generated method stub
		final JarOutputStream 	jos = new JarOutputStream(os);
		final JsonNode			root = loadContentDescriptor(fsi);
	
		dumpContentAsIs(jos, "META-INF/", NULL_CONTENT);
		dumpContentAsIs(jos, "META-INF/MANIFEST.MF", AdminUtils.class.getResourceAsStream("manifest.mf"));
		dumpContentAsIs(jos, Navigator.class.getCanonicalName().replace('.', '/')+".class", Navigator.class.getResourceAsStream("Navigator.class"));
		dumpContentAsIs(jos, Navigator.class.getPackage().getName().replace('.', '/')+"/avatar.jpg", Navigator.class.getResourceAsStream("avatar.jpg"));
		dumpContentAsIs(jos, NavigatorHandler.class.getCanonicalName().replace('.', '/')+".class", NavigatorHandler.class.getResourceAsStream("NavigatorHandler.class"));
		dumpContentAsIs(jos, "index.html", buildIndexHtml(root));
		if (javaDocLocation != null) {
			dumpJavaDoc(javaDocLocation, javaDocLocation.getAbsolutePath(), jos);
		}
		jos.finish();
	}

	private static void packWarProject(final FileSystemInterface fsi, final OutputStream os, final File javaDocLocation) throws IOException, ContentException, NullPointerException, URISyntaxException {
		// TODO Auto-generated method stub
		final JarOutputStream 	jos = new JarOutputStream(os);
		final JsonNode			root = loadContentDescriptor(fsi);
		
		dumpContentAsIs(jos, "WEB-INF/", NULL_CONTENT);
		dumpContentAsIs(jos,  "WEB-INF/web.xml", AdminUtils.class.getResourceAsStream("web.xml"));
		dumpContentAsIs(jos, "index.html", buildIndexHtml(root));
		if (javaDocLocation != null) {
			dumpJavaDoc(javaDocLocation, javaDocLocation.getAbsolutePath(), jos);
		}
		jos.finish();
	}

	private static InputStream buildIndexHtml(final JsonNode node) throws IOException, URISyntaxException {
		final StringBuilder	sb = new StringBuilder();
		
		buildProlog(sb);
		buildNavigation(node, sb);
		buildEpilog(sb);
		
		return new ByteArrayInputStream(sb.toString().getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING));
	}

	private static void buildProlog(final StringBuilder sb) throws IOException, URISyntaxException   {
		sb.append(URIUtils.loadCharsFromURI(AdminUtils.class.getResource("prolog.html").toURI())).append("<ul>");
	}

	private static void buildNavigation(final JsonNode node, final StringBuilder sb) {
		// TODO Auto-generated method stub  http://htmlbook.ru/samlayout/verstka-na-html5/shapka-stranitsy
		switch (node.getType()) {
			case JsonObject :
				sb.append("<li>").append(node.getChild(F_CAPTION).getStringValue());
				if (ContentNodeType.valueOf(node.getChild(F_TYPE).getStringValue()).getGroup() == ContentNodeGroup.SUBTREE) {
					sb.append("<br><ul>");
					buildNavigation(node.getChild(F_CONTENT), sb);
					sb.append("</ul>");
				}
				sb.append("</li>");
				break;
			case JsonArray 	:
				for (JsonNode item : node.children()) {
					buildNavigation(item, sb);
				}
				break;
			default :
				sb.append("<li>").append(node.getStringValue()).append("</li>");
				break;
		}
	}

	private static void buildEpilog(final StringBuilder sb) throws IOException, URISyntaxException {
		sb.append("</ul>").append(URIUtils.loadCharsFromURI(AdminUtils.class.getResource("epilog.html").toURI()));
	}
	
	private static void dumpJavaDoc(final File node, final String parentPath, final JarOutputStream jos) throws IOException {
		if (node.isDirectory()) {
			dumpContentAsIs(jos, "/javadoc/"+extractPartName(node, parentPath), NULL_CONTENT);
			node.listFiles((f)->{
				try{dumpJavaDoc(f, parentPath, jos);
				} catch (IOException e) {
				}
				return false;
			});
		}
		else {
			try(final InputStream	is = new FileInputStream(node)) {
				dumpContentAsIs(jos, "/javadoc/"+extractPartName(node, parentPath), is);
			}
		}
	}
	
	private static void dumpContentAsIs(final JarOutputStream jos, final String partName, final InputStream content) throws IOException {
		final JarEntry	je = new JarEntry(partName) {{setMethod(DEFLATED);}};

		jos.putNextEntry(je);
		Utils.copyStream(content, jos);
		jos.closeEntry();
	}
	
	private static String extractPartName(final File file, final String parentName) {
		if (file.isDirectory()) {
			return file.getAbsolutePath().substring(parentName.length()).replace('\\', '/')+'/';
		}
		else {
			return file.getAbsolutePath().substring(parentName.length()).replace('\\', '/');
		}
	}
}
