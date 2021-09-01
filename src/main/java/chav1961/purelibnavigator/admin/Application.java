package chav1961.purelibnavigator.admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.ui.interfaces.UIItemState;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelibnavigator.admin.ContentEditorAndViewer.ContentType;
import chav1961.purelibnavigator.interfaces.ContentNodeGroup;
import chav1961.purelibnavigator.interfaces.ContentNodeType;

public class Application extends JFrame implements LocaleChangeListener, AutoCloseable {
	private static final long 					serialVersionUID = -3061028320843379171L;

	public static final String					ARG_HELP_PORT = "helpport";
	public static final String					APP_SETTINGS_FILE = "./.admin.properties";

	private static final String					APPLICATION_TITLE = "Application.title";
	private static final String					MESSAGE_FILE_LOADED = "Application.message.fileLoaded";
	private static final String					MESSAGE_FILE_SAVED = "Application.message.fileSaved";
	
	private static final String					APPLICATION_HELP_TITLE = "Application.help.title";
	private static final String					APPLICATION_HELP_CONTENT = "Application.help.content";

	private static final String					APPLICATION_SAVE_TITLE = "Application.save.title";
	private static final String					APPLICATION_SAVE_CONTENT = "Application.save.content";
	private static final String					APPLICATION_HELP_PROJECTS = "Application.help.projects";
	
	
	private final ContentMetadataInterface		mdi;
	private final Localizer						localizer;
	private final int 							localHelpPort;
	private final CountDownLatch				latch;
	private final JMenuBar						menu;
	private final StaticTreeContent				stc;
	private final ContentEditorAndViewer		ceav;
	private final JStateString					state;
	private final Set<File>						temporaries = new HashSet<>();
	private final AppSettings					as;
	private final AutoBuiltForm<AppSettings>	form;
	
	private FileSystemInterface				fsi;
	private String							creoleContent = null;
	
	public Application(final ContentMetadataInterface mdi, final Localizer parent, final int localHelpPort, final CountDownLatch latch) throws EnvironmentException, NullPointerException, IllegalArgumentException, IOException, ContentException {
		if (mdi == null) {
			throw new NullPointerException("Application descriptor can't be null");
		}
		else if (parent == null) {
			throw new NullPointerException("Parent localizer can't be null");
		}
		else if (latch == null) {
			throw new NullPointerException("Latch to notify closure can't be null");
		}
		else {
			this.mdi = mdi;
			this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
			this.localHelpPort = localHelpPort;
			this.latch = latch;
			this.state = new JStateString(this.localizer,10);
			
			parent.push(localizer);
			localizer.addLocaleChangeListener(this);
			this.menu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class, (meta)->getAccessAndVisibility(meta));
			
			SwingUtils.assignActionListeners(menu,this);
			
			this.as = new AppSettings(state);
			this.form = new AutoBuiltForm<AppSettings>(ContentModelFactory.forAnnotatedClass(AppSettings.class), localizer, PureLibSettings.INTERNAL_LOADER, as, as);
			this.form.setPreferredSize(new Dimension(300,110));
			
			final File	f = new File(APP_SETTINGS_FILE);
			
			if (f.exists() && f.isFile()) {
				try(final InputStream	is = new FileInputStream(f)) {
					this.as.load(is);
				}
			}
			
			final JSplitPane	splitter = new JSplitPane();
			final JPanel		rightPanel = new JPanel(new BorderLayout());
			
			this.fsi = null;
			this.ceav = new ContentEditorAndViewer(localizer, state, mdi, (t)->saveCreoleContent(t));
			this.stc = new StaticTreeContent(mdi, localizer, state
										,(item,node)->{
											if (creoleContent != null && ceav.creoleContentWasChanged()) {
												ceav.saveCreoleContent();
											}
											
											ContentNodeType	type;
											
											if (node != null && (type = ContentNodeType.valueOf(node.getChild(AdminUtils.F_TYPE).getStringValue())).getGroup() == ContentNodeGroup.LEAF) {
												
												try(final FileSystemInterface	content = fsi.clone().open("/"+node.getChild(AdminUtils.F_ID).getStringValue()+type.getFileNameSuffix())) {
													if (content.exists() && content.isFile()) {
														switch (type) {
															case CREOLE	:
																creoleContent = content.getPath();
																
																try(final Reader		rdr = content.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
																	
																	ceav.getCreoleEditor().setText(Utils.fromResource(rdr));
																	ceav.setContentType(ContentType.CREOLE);
																}
																break;
															case IMAGE	:
																try(final InputStream	is = content.read()) {
																	final Image			image = ImageIO.read(is);
																	
																	ceav.getImageContainer().setBackground(image);
																	creoleContent = null; 
																	ceav.setContentType(ContentType.IMAGE);
																}
																break;
															default:
																creoleContent = null; 
																ceav.setContentType(ContentType.COMMON);
																break;
														}
													}
													else {
														creoleContent = null; 
														ceav.setContentType(ContentType.COMMON);
													}
												} catch (IOException exc) {
													state.message(Severity.error, exc.getLocalizedMessage(), exc);
												}
											}
											else {
												ceav.setContentType(ContentType.COMMON);
											}
										});
			
			rightPanel.add(ceav, BorderLayout.CENTER);
			splitter.setLeftComponent(new JScrollPane(stc));
			splitter.setRightComponent(rightPanel);
			splitter.setDividerLocation(200);
			
			getContentPane().add(menu,BorderLayout.NORTH);
			getContentPane().add(splitter,BorderLayout.CENTER);
			getContentPane().add(state,BorderLayout.SOUTH);

			SwingUtils.centerMainWindow(this,0.75f);
			SwingUtils.assignExitMethod4MainWindow(this,()->exitApplication());
			localizer.addLocaleChangeListener(this);
			fillLocalizedStrings();
			
			newFile();
		}
	}

	@Override
	public void close() throws RuntimeException {
		localizer.removeLocaleChangeListener(this);
		for (File item : temporaries) {
			Utils.deleteDir(item);
		}
		latch.countDown();
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(menu, oldLocale, newLocale);
		SwingUtils.refreshLocale(stc, oldLocale, newLocale);
		SwingUtils.refreshLocale(ceav, oldLocale, newLocale);
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
	}
	
	private void saveCreoleContent(final String content) throws IOException {
		try(final FileSystemInterface	creole = fsi.clone().open(creoleContent)) {
			try(final Writer			wr = creole.create().charWrite(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
				Utils.copyStream(new StringReader(content), wr);
			}
		}
	}

	@OnAction("action:/newFile")
	private void newFile() {
		if (saveAndNeedContinue()) {
			try{final File	f = new File(System.getProperty("java.io.tmpdir"), "nav"+System.currentTimeMillis());
			
				f.mkdirs();
				temporaries.add(f);
				
				setFileSystem(FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":"+f.toURI())));
			} catch (IOException  e) {
				printError(e);
			}
		}
	}
	
	@OnAction("action:/openFile")
	private void openFile() {
		if (saveAndNeedContinue()) {
			try(final FileSystemInterface	total = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"))){
				
				for (String item : JFileSelectionDialog.select(this, localizer, total, JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_CAN_SELECT_DIR)) {
					final URI	loadedURI = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":"+total.getAbsoluteURI().toString()+"/"+item);
					
					setFileSystem(FileSystemFactory.createFileSystem(loadedURI));
					break;
				}
			} catch (LocalizationException | IOException e) {
				printError(e);
			}
		}
	}

	@OnAction("action:/saveFile")
	private void saveFile() {
		try{if (stc.treeWasModified()) {
				stc.save(fsi);
			}
			if (ceav.creoleContentWasChanged()) {
				ceav.saveCreoleContent();
			}
		} catch (ContentException e) {
			printError(e);
		}
	}

	@OnAction("action:/saveFileAs")
	private void saveFileAs() {
		saveFile();
		try(final FileSystemInterface	total = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"))){
			
			for (String item : JFileSelectionDialog.select(this, localizer, total, JFileSelectionDialog.OPTIONS_FOR_SAVE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_CAN_SELECT_DIR)) {
				final URI					storedURI = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":"+total.getAbsoluteURI().toString()+"/"+item);
				final FileSystemInterface	stored = FileSystemFactory.createFileSystem(storedURI);
					
				fsi.copy(stored);
				fsi.close();
				fsi = stored;
				break;
			}
		} catch (LocalizationException | IOException e) {
			printError(e);
		}
	}
	
	@OnAction("action:/exit")
	private void exitApplication() {
		if (saveAndNeedContinue()) {
			setVisible(false);
			dispose();
			latch.countDown();
		}
	}

	@OnAction("action:/pack")
	private void packProject() {
		if (saveAndNeedContinue()) {
			try(final FileSystemInterface	total = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file://./"))){
				
				for (String item : JFileSelectionDialog.select(this, localizer, total, JFileSelectionDialog.OPTIONS_FOR_SAVE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_CONFIRM_REPLACEMENT, FilterCallback.of(APPLICATION_HELP_PROJECTS, "*.hlp"))) {
					try(final FileSystemInterface	store = total.clone().open(item).create();
						final OutputStream			os = store.write()) {
						
						AdminUtils.packProject(fsi, os, as);
					}
					state.message(Severity.info, "Project was packed to ["+item+"] successfully");
					break;
				}
			} catch (LocalizationException | IOException e) {
				printError(e);
			}
		}
	}	
	
	@OnAction("action:/builtin.languages")
	private void selectLang(final Map<String,String[]> map) throws LocalizationException, NullPointerException {
		localizer.getParent().setCurrentLocale(Locale.forLanguageTag(map.get("lang")[0]));
	}

	@OnAction("action:/settings")
	private void appSettings() {
		try{if (AutoBuiltForm.ask(this, localizer, form)) {
				final File	f = new File(APP_SETTINGS_FILE);
				
				if (f.exists() && f.isDirectory()) {
					throw new IOException("Config name ["+f.getAbsolutePath()+"] can't be created - directory  with thes name already exists");
				}
				else {
					try(final OutputStream	os = new FileOutputStream(f)) {
						this.as.save(os);
					}
				}
			}
		} catch (LocalizationException | IOException  e) {
			printError(e);
		}		
	}	
	
	@OnAction("action:/helpAbout")
	private void about() throws LocalizationException, URISyntaxException {
		SwingUtils.showAboutScreen(this, localizer, APPLICATION_HELP_TITLE, APPLICATION_HELP_CONTENT, this.getClass().getResource("favicon.png").toURI(), new Dimension(300,300));
	}

	private void setFileSystem(final FileSystemInterface fsi) {
		this.fsi = fsi;
		
		try(final FileSystemInterface	temp = fsi.clone().open("/"+AdminUtils.CONTENT_FILE)){
		
			if (!temp.exists()) {
				try(final InputStream			is = this.getClass().getResourceAsStream(AdminUtils.CONTENT_FILE);
					final OutputStream			os = temp.create().write()) {
					
					Utils.copyStream(is, os);
				}
			}
			stc.setFileSystem(fsi);
		} catch (IOException | ContentException e) {
			printError(e);
		}
	}

	private boolean saveAndNeedContinue() {
		if ((stc.treeWasModified() || ceav.creoleContentWasChanged())) {
			try{switch (new JLocalizedOptionPane(localizer).confirm(this, APPLICATION_SAVE_CONTENT, APPLICATION_SAVE_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
					case JOptionPane.YES_OPTION 	:
						saveFile();
					case JOptionPane.NO_OPTION 		:
						return true;
					case JOptionPane.CANCEL_OPTION	:
						return false;
				}
			} catch (LocalizationException e) {
				printError(e);
			}
		}
		return true;						
	}

	private UIItemState.AvailableAndVisible getAccessAndVisibility(final ContentNodeMetadata meta) {
		if (URI.create("app:action:/saveFile").equals(meta.getApplicationPath())) {
			return stc.treeWasModified() || ceav.creoleContentWasChanged() ? UIItemState.AvailableAndVisible.AVAILABLE : UIItemState.AvailableAndVisible.NOTAVAILABLE;
		}
		else {
			return UIItemState.AvailableAndVisible.DEFAULT;
		}
	}

	private void fillLocalizedStrings() throws LocalizationException {
		setTitle(localizer.getValue(APPLICATION_TITLE));
	}

	private void printError(final Exception e) {
		state.message(Severity.error, e.getLocalizedMessage(), e);
	}
	
	private static int getFreePort() throws IOException {
		try (ServerSocket 	socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	
	
	public static void main(String[] args) throws NullPointerException, IllegalArgumentException {
		try{final ArgParser						parser = new ApplicationArgParser().parse(args);
		
			final int							helpPort = !parser.isTyped(ARG_HELP_PORT) ? getFreePort() : parser.getValue(ARG_HELP_PORT, int.class);
			
			final SubstitutableProperties		props = new SubstitutableProperties(Utils.mkProps(
													 NanoServiceFactory.NANOSERVICE_PORT, ""+helpPort
													,NanoServiceFactory.NANOSERVICE_ROOT, "fsys:xmlReadOnly:root://"+Application.class.getCanonicalName()+"/chav1961/purelibnavigator/admin/helptree.xml"
													,NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, Application.class.getResource("prolog.cre").toString() 
													,NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, Application.class.getResource("epilog.cre").toString() 
												));
		
			try(final InputStream				is = Application.class.getResourceAsStream("application.xml");
				final Localizer					localizer = new PureLibLocalizer(); ){
//				final NanoServiceFactory		service = new NanoServiceFactory(logger,props)) {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				final CountDownLatch			latch = new CountDownLatch(1);
				
				try(final Application		newApp = new Application(xda,localizer,helpPort,latch)) {
//					service.start();
					newApp.setVisible(true);
					latch.await();
//					service.stop();
				}
			} catch (IOException | EnvironmentException | InterruptedException  e) {
				e.printStackTrace();
				System.exit(129);
			}
		} catch (CommandLineParametersException e) {
			e.printStackTrace();
			System.exit(128);
		} catch (IOException | ContentException e) {
			e.printStackTrace();
			System.exit(129);
		}
		System.exit(0);
	}

	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new IntegerArg(ARG_HELP_PORT,false,"help system port",0));
		}
	}

}
