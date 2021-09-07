package chav1961.purelibnavigator.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;
import chav1961.purelibnavigator.admin.entities.AppSettings;
import chav1961.purelibnavigator.interfaces.ContentNodeGroup;
import chav1961.purelibnavigator.interfaces.ContentNodeType;
import chav1961.purelibnavigator.interfaces.ResourceType;
import chav1961.purelibnavigator.navigator.Navigator;
import chav1961.purelibnavigator.navigator.NavigatorHandler;

public class AdminUtils {
	public static final String			CONTENT_FILE = "content.json";

	public static final String			F_NAVIGATION = "navigation";
	public static final String			F_RESOURCES = "resources";
	public static final String			F_ID = "id";
	public static final String			F_TYPE = "type";
	public static final String			F_CAPTION = "caption";
	public static final String			F_CONTENT = "content";
	
	private static final InputStream	NULL_CONTENT = new InputStream() {@Override public int read() throws IOException {return -1;}}; 
	
	
	// JSON  format:
	// {
	//	"navigation":{
	//		{"id":<resource id>,"type":"<type>","caption":"<caption>","content":[<repeats tree node>]}
	//	}, 
	// 	"resources":[
	//		{"type": "<type>", "content":[<resource id>, ...]}, ...
	//	]
	// }
	
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
	
	public static boolean buildInternalLinksMenu(final JMenu container, final String creoleContent, final String linkPrefix) {
		if (container == null) {
			throw new NullPointerException("Menu container can't be null"); 
		}
		else if (creoleContent == null) {
			throw new NullPointerException("Content to parse can't be null"); 
		}
		else if (!creoleContent.trim().isEmpty()) {
			final boolean[]					result = {false};
			
			try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length)->{
															for(int index = 0; index < length; index++) {
																if (data[from + index] == '=') {
																	int lastIndex = length-1;
																	
																	while (data[from + index] == '=') {
																		index++;
																	}
																	while ((Character.isWhitespace(data[from + lastIndex]) || data[from + lastIndex] == '=') && lastIndex > index) {
																		lastIndex--;
																	}
																	
																	if (++lastIndex > index) {
																		final String	item = new String(data, from + index, from + lastIndex - index).trim();
																		final JMenuItem	menuItem = new JMenuItem("#"+item);
																		
																		menuItem.setActionCommand("[["+linkPrefix+item+"|"+item+"]]");
																		container.add(menuItem);
																		result[0] = true;
																	}
																	return;
																}
																else if (!Character.isWhitespace(data[index])) {
																	return;
																}
															}
														})) {
				final char[]				charContent = creoleContent.toCharArray(); 
				
				lblp.write(charContent, 0, charContent.length);
			} catch (IOException | SyntaxException e) {
			}
			return result[0];
		}
		else {
			return false;
		}
	}
	
	public static boolean buildSiblingLinksMenu(final JMenu container, final JsonNode parent, final Predicate<JsonNode> include) {
		if (container == null) {
			throw new NullPointerException("Menu container can't be null"); 
		}
		else if (parent == null) {
			throw new NullPointerException("Parent node can't be null"); 
		}
		else if (include == null) {
			throw new NullPointerException("Inlude callback can't be null"); 
		}
		else {
			boolean	result = false;
			
			for (JsonNode item : parent.getChild(F_CONTENT).children()) {
				if (include.test(item)) {
					container.add(createMenuItemByJsonNode(item,""));
					result = true;
				}
			}
			return result;
		}
	}
	
	public static boolean buildTreeLinksMenu(final JMenu container, final JsonNode node, final Predicate<JsonNode> include) {
		boolean	result = false;
		
		for(ResourceType item : new ResourceType[] {ResourceType.CREOLE, ResourceType.IMAGE, ResourceType.RESOURCE}) {
			final JMenu	menuItem = new JMenu(item.name());
			
			if (buildTreeLinksMenu(menuItem, node, item, include)) {
				menuItem.setIcon(item.getIcon());
				container.add(menuItem);
				result = true;
			}
		}
		return result;
	}

	private static boolean buildTreeLinksMenu(final JMenu container, final JsonNode node, final ResourceType resourceType, final Predicate<JsonNode> include) {
		final ContentNodeType	type = ContentNodeType.valueOf(node.getChild(F_TYPE).getStringValue());
		boolean					result = false;
		
		if (type.getGroup() == ContentNodeGroup.SUBTREE && node.hasName(F_CONTENT)) {
			for (JsonNode item : node.getChild(F_CONTENT).children()) {
				final JMenu		subtree = new JMenu(item.getChild(F_CAPTION).getStringValue());
				
				if (buildTreeLinksMenu(subtree, item, resourceType, include)) {
					container.add(subtree);
					result = true;
				}
			}
		}
		if (type.getResourceType() == resourceType) {
			if (include.test(node)) {
				if (result) {
					container.addSeparator();
				}
				container.add(createMenuItemByJsonNode(node,""));
				result = true;
			}
		}
		return result;
	}	

	private static JMenuItem createMenuItemByJsonNode(final JsonNode node, final String prefix) {
		final ContentNodeType	type = ContentNodeType.valueOf(node.getChild(F_TYPE).getStringValue());
		final String			id = node.getChild(F_ID).getStringValue();
		final String			caption = node.getChild(F_CAPTION).getStringValue();
		final JMenuItem			menuItem = new JMenuItem(caption);
		
		menuItem.setActionCommand("[["+prefix+id+type.getResourceType().getResourceSuffix()+"|"+caption+"]]");
		return menuItem;
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
		dumpContentAsIs(jos, Navigator.class.getPackage().getName().replace('.', '/')+"/notFound.html", Navigator.class.getResourceAsStream("notFound.html"));
		dumpContentAsIs(jos, Navigator.class.getPackage().getName().replace('.', '/')+"/illegalRequest.html", Navigator.class.getResourceAsStream("illegalRequest.html"));
		dumpContentAsIs(jos, NavigatorHandler.class.getCanonicalName().replace('.', '/')+".class", NavigatorHandler.class.getResourceAsStream("NavigatorHandler.class"));
		dumpContentAsIs(jos, "index.html", buildIndexHtml(root.getChild(F_NAVIGATION)));
		try (final FileSystemInterface	item = fsi.clone()) {
			item.list(".*\\.cre",  (f)->{
				try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
					try(final Writer			wr = new OutputStreamWriter(baos, PureLibSettings.DEFAULT_CONTENT_ENCODING);
						final CreoleWriter		cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML, 
															(wrP, instP)-> {((Writer)wrP).write(""); return false;}, 
															(wrE, instE)-> {((Writer)wrE).write(""); return false;});
						final Reader			rdr = f.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
					
						Utils.copyStream(rdr, cwr);
					}
					dumpContentAsIs(jos, f.getName(), new ByteArrayInputStream(baos.toByteArray()));
				}
				return ContinueMode.CONTINUE;
			});
		}
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
		sb.append("<nav><ul>");
		buildNavigation(node, sb);
		sb.append("</ul></nav>");
		buildEpilog(sb);
		
		return new ByteArrayInputStream(sb.toString().getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING));
	}

	private static void buildProlog(final StringBuilder sb) throws IOException, URISyntaxException   {
		sb.append(URIUtils.loadCharsFromURI(AdminUtils.class.getResource("prolog.html").toURI()));
	}

	private static void buildNavigation(final JsonNode node, final StringBuilder sb) {
		// TODO Auto-generated method stub  http://htmlbook.ru/samlayout/verstka-na-html5/shapka-stranitsy
		switch (node.getType()) {
			case JsonObject :
				final ContentNodeType	type = ContentNodeType.valueOf(node.getChild(F_TYPE).getStringValue()); 
				
				sb.append("<li>");
				if (type.getResourceType().hasResource()) {
					sb.append("<a href=\"#\" onClick=\"setEmbedRef('/").append(node.getChild(F_ID).getStringValue()).append(type.getResourceType().getResourceSuffix()).append("')\">");
				}
				sb.append(node.getChild(F_CAPTION).getStringValue());
				
				if (type.getResourceType().hasResource()) {
					sb.append("</a>");
				}
				if (ContentNodeType.valueOf(node.getChild(F_TYPE).getStringValue()).getGroup() == ContentNodeGroup.SUBTREE) {
					sb.append("<br><ul>");
					buildNavigation(node.getChild(F_CONTENT), sb);
					sb.append("</ul>");
				}
				if (type.getResourceType().hasResource()) {
					sb.append("</a>");
				}
				sb.append("</li>");
				break;
			case JsonArray 	:
				for (JsonNode item : node.children()) {
					buildNavigation(item, sb);
				}
				break;
			default :
				sb.append("<li><a href=\"#\">").append(node.getStringValue()).append("</a></li>");
				break;
		}
	}

	private static void buildEpilog(final StringBuilder sb) throws IOException, URISyntaxException {
		sb.append(URIUtils.loadCharsFromURI(AdminUtils.class.getResource("epilog.html").toURI()));
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
