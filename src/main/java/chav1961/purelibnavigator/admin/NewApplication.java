package chav1961.purelibnavigator.admin;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelibnavigator.admin.ContentEditorAndViewer.ContentType;

public class NewApplication extends JFrame implements LocaleChangeListener {
	private static final long 				serialVersionUID = -3061028320843379171L;

	public static final String				ARG_HELP_PORT = "helpport";
	
	public static final String				APPLICATION_TITLE = "Application.title";
	public static final String				MESSAGE_FILE_LOADED = "Application.message.fileLoaded";
	public static final String				MESSAGE_FILE_SAVED = "Application.message.fileSaved";
	
	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final int 						localHelpPort;
	private final CountDownLatch			latch;
	private final JMenuBar					menu;
	private final StaticTreeContent			stc;
	private final ContentEditorAndViewer	ceav;
	private final JStateString				state;
	
	public NewApplication(final ContentMetadataInterface mdi, final Localizer parent, final int localHelpPort, final CountDownLatch latch) throws EnvironmentException, NullPointerException, IllegalArgumentException, IOException, ContentException, ClassNotFoundException {
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
			this.menu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			
			SwingUtils.assignActionListeners(menu,this);
			
			final JSplitPane	splitter = new JSplitPane();
			final JPanel		rightPanel = new JPanel(new BorderLayout());
			
			this.stc = new StaticTreeContent(mdi, localizer, state, FileSystemFactory.createFileSystem(URI.create("fsys:"+new File("./src/test/resources").toURI()))
										,(item,node)->{
											//System.err.println("Node="+node);
										});
			this.ceav = new ContentEditorAndViewer(localizer, state, mdi);
			
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
		}
	}
	
	@OnAction("action:/exit")
	private void exitApplication () {
		setVisible(false);
		localizer.removeLocaleChangeListener(this);
		dispose();
		latch.countDown();
	}
	
	@OnAction("action:/builtin.languages")
	private void selectLang(final Map<String,String[]> map) throws LocalizationException, NullPointerException {
		localizer.getParent().setCurrentLocale(Locale.forLanguageTag(map.get("lang")[0]));
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(menu, oldLocale, newLocale);
		SwingUtils.refreshLocale(stc, oldLocale, newLocale);
		SwingUtils.refreshLocale(ceav, oldLocale, newLocale);
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
	}

	private void fillLocalizedStrings() throws LocalizationException {
		setTitle(localizer.getValue(APPLICATION_TITLE));
	}

	public static void main(String[] args) throws NullPointerException, IllegalArgumentException, ClassNotFoundException {
		try{final ArgParser						parser = new ApplicationArgParser().parse(args);
		
			final int							helpPort = !parser.isTyped(ARG_HELP_PORT) ? getFreePort() : parser.getValue(ARG_HELP_PORT, int.class);
			
			final SubstitutableProperties		props = new SubstitutableProperties(Utils.mkProps(
													 NanoServiceFactory.NANOSERVICE_PORT, ""+helpPort
													,NanoServiceFactory.NANOSERVICE_ROOT, "fsys:xmlReadOnly:root://"+NewApplication.class.getCanonicalName()+"/chav1961/purelibnavigator/admin/helptree.xml"
													,NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, Application.class.getResource("prolog.cre").toString() 
													,NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, Application.class.getResource("epilog.cre").toString() 
												));
		
			try(final InputStream				is = Application.class.getResourceAsStream("application.xml");
				final Localizer					localizer = new PureLibLocalizer(); ){
//				final NanoServiceFactory		service = new NanoServiceFactory(logger,props)) {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				final CountDownLatch			latch = new CountDownLatch(1);
				
				new NewApplication(xda,localizer,helpPort,latch).setVisible(true);
//				service.start();
				latch.await();
//				service.stop();
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

	private static int getFreePort() throws IOException {
		try (ServerSocket 	socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}
	
	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new IntegerArg(ARG_HELP_PORT,false,"help system port",0));
		}
	}
	
}
