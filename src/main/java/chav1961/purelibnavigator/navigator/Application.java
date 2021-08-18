package chav1961.purelibnavigator.navigator;

import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.NullLoggerFacade;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JSystemTray;

/**
 * <p>This class is an application class for Pure Library Navigator.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class Application implements LocaleChangeListener {
	public static final String	HELP_USING = "Use java -jar purelib.navigator.jar {-http <port>|[<-swing>]} {[-local]|-external} [-shutdown] [-lang {ru|en}]";
	public static final String	UNKNOWN_ARGUMENT = "Unknown command line argument [%1$s]";
	public static final String	ILLEGAL_HTTP_PORT = "Illegal HTTP port [%1$s]: need be any valid number in the range 1..65535";
	public static final String	HTTP_PORT_MISSING = "Mandatory HTTP port is not typed for '%1$s' command line argument";
	public static final String	LANGUAGE_MISSING = "Mandatory language name is not typed for '-lang' command line argument";
	public static final String	MUTUALLY_EXCLUSIVE_PARAMETERS = "Mutually exclusive parameters %1$s and %2$s were typed";
	public static final String	SHUTDOWN_REQUIRES_HTTP = "Using %1$s parameter requires to type %2$s also";
	
	public static final String	KEY_DEBUG = "debug";
	public static final String	KEY_HTTP = "http";
	public static final String	KEY_SWING = "swing";
	public static final String	KEY_LOCAL = "local";
	public static final String	KEY_EXTERNAL = "external";
	public static final String	KEY_SHUTDOWN = "shutdown";
	public static final String	KEY_LANG = "lang";
	
	public static final String	APPLICATION_XML = "application.xml";
	public static final String	IMAGE_NAME = "avatar.jpg";
	public static final String	IMAGE_TOOLTIP = "chav1961.purelibnavigator.navigator.Application.tt";

	private final Localizer			localizer;
	private final JSystemTray		tray;
	private final int				port;
	private final CountDownLatch	latch = new CountDownLatch(1);

	public Application(final Localizer localizer, final JSystemTray tray, final int port) {
		this.localizer = localizer;
		this.tray = tray;
		this.port = port;
		localizer.addLocaleChangeListener((o,n)->localeChanged(o, n));
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		tray.localeChanged(oldLocale, newLocale);
	}
	
	/**
	 * <p>Main method to start application. Available command line arguments are:</p>
	 * <ul>
	 * <li>{<b>-http</b> <port>|<b>-swing</b>} - start this application as local HTTP server on the computer or as standalone Swing-based application. Default is <b>-swing</b></li> 
	 * <li>{<b>-local</b>|<b>-external</b>} - use local or external knowledge database (Internet connection requires). Default is <b>-local</b></li> 
	 * <li><b>-shutdown</b> - stop local HTTP server was started earlier</li> 
	 * <li><b>-lang</b> {<b>ru</b>|<b>en</b>} - use given localization in the application. Default localization appropriates your computer settings. Any unsupported localization is treated as <b>en</b></li> 
	 * </ul>
	 * <p>Return codes from application are:</p>
	 * <ul>
	 * <li>0 - application completed successfully</li>
	 * <li>1 - command line parameters are not typed</li>
	 * <li>128 - any errors in command line parameters</li>
	 * <li>129 - any errors during execution</li>
	 * </ul>
	 * 
	 * @param args command line arguments
	 */
	public static void main(final String[] args) {
		final ArgParser		parser = new ApplicationArgParser();
		
		try{final ArgParser	parsedString = parser.parse(args);

			if (args.length == 0) {
				System.err.println(parser.getUsage("purelib.navigator.jar"));
				System.exit(1);
			}
			else {
				if (parsedString.isTyped(KEY_HTTP) && parsedString.isTyped(KEY_SWING)) {
					System.err.println(String.format(MUTUALLY_EXCLUSIVE_PARAMETERS,KEY_HTTP,KEY_SWING));
					printAndExit(128);
				}
				else if (parsedString.isTyped(KEY_LOCAL) && parsedString.isTyped(KEY_EXTERNAL)) {
					System.err.println(String.format(MUTUALLY_EXCLUSIVE_PARAMETERS,KEY_LOCAL,KEY_EXTERNAL));
					printAndExit(128);
				}
				else if (parsedString.isTyped(KEY_SHUTDOWN) && !parsedString.isTyped(KEY_HTTP)) {
					System.err.println(String.format(SHUTDOWN_REQUIRES_HTTP,KEY_SHUTDOWN,KEY_HTTP));
					printAndExit(128);
				}
				else if (parsedString.isTyped(KEY_HTTP) && parsedString.isTyped(KEY_SHUTDOWN)) {
					System.exit(stopServer(parsedString.getValue(KEY_HTTP,int.class)));
				}
				else if (parsedString.isTyped(KEY_HTTP) && !parsedString.isTyped(KEY_SHUTDOWN)) {
					startServer(parsedString.getValue(KEY_HTTP,int.class),parsedString.getValue(KEY_LANG,SupportedLanguages.class).toString());
				}
				else {
					startGUI(parsedString);
				}
			}
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.err.println(parser.getUsage("purelib.navigator.jar"));
			System.exit(128);
		}
	}
	
	private static int startGUI(final ArgParser parser) throws CommandLineParametersException {
		Locale.setDefault(new Locale(parser.getValue(KEY_LANG,SupportedLanguages.class).name()));
		
		try{final Localizer						purelibLocalizer = new PureLibLocalizer();
			
			try(final InputStream				is = Application.class.getResourceAsStream(APPLICATION_XML);
				final LoggerFacade				logger = parser.getValue(KEY_DEBUG,boolean.class) ? new SystemErrLoggerFacade() : new NullLoggerFacade()) {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				
				new GUIApplication(xda,purelibLocalizer).setVisible(true);
			}
			return 0;
		} catch (EnvironmentException | ContentException | IOException exc) {
			exc.printStackTrace();
			return 129;
		}
	}

	private static int startServer(final int httpPort, final String lang) {
		try{final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(Application.class.getResourceAsStream(APPLICATION_XML));
			final Localizer					localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
			
			PureLibSettings.PURELIB_LOCALIZER.push(localizer);
			PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(Locale.forLanguageTag(lang));

			final JPopupMenu				menu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JPopupMenu.class);
			
			try(final JSystemTray			tray = new JSystemTray(localizer, "Navigator", Application.class.getResource(IMAGE_NAME).toURI(), IMAGE_TOOLTIP, menu)) {
				final Application			app = new Application(localizer, tray, httpPort);
				final ActionListener		al = (e)->app.showHelp(); 

				localizer.setCurrentLocale(Locale.forLanguageTag(lang));
				SwingUtils.assignActionListeners(menu, app);
				tray.addActionListener(al);
				
				if (!Desktop.isDesktopSupported()) {
					tray.message(Severity.warning, "Java desktop is not supported. Start browser manually with http://localhost:"+httpPort+" address URI");
				}
				else {
					tray.message(Severity.info, "Test navigator started");
				}
				app.waitShutdown();
				tray.removeActionListener(al);
			}
			return 0;
		} catch (final EnvironmentException | URISyntaxException exc) {
			exc.printStackTrace();
			return 129;
		}
	}

	private static int stopServer(final int httpPort) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static void printAndExit(final int rc) {
		System.err.println(HELP_USING);
		System.exit(1);
	}
	
	private void showHelp() {
		if (Desktop.isDesktopSupported()) {
			try{Desktop.getDesktop().browse(URI.create("http://localhost:"+port));
			} catch (IOException e) {
				tray.message(Severity.error, "Error starting browser: "+e.getMessage());
			}
		}
	}

	private void waitShutdown() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@OnAction("action:/refresh")
	private void refresh() {
		
	}

	@OnAction("action:/builtin.languages")
	private void selectLang(final Hashtable<String,String[]> langs) throws LocalizationException {
		localizer.setCurrentLocale(SupportedLanguages.valueOf(langs.get("lang")[0]).getLocale());
	}
	
	@OnAction("action:/exit")
	private void exit() {
		latch.countDown();
	}
	
	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
					 new BooleanArg(KEY_DEBUG, false, "turn on debugging trace", false)
					,new IntegerArg(KEY_HTTP, false, "local http port to connect to", 8080)
					,new BooleanArg(KEY_SWING, false, "Use Java Swing instead of WEB browser", false)
					,new BooleanArg(KEY_LOCAL, false, "turn on debugging trace", false)
					,new BooleanArg(KEY_EXTERNAL, false, "turn on debugging trace", false)
					,new BooleanArg(KEY_SHUTDOWN, false, "turn on debugging trace", false)
					,new EnumArg<SupportedLanguages>(KEY_LANG, SupportedLanguages.class, false, "select default language supported", SupportedLanguages.ru)
				};
		
		ApplicationArgParser() {
			super(KEYS);
		}
	}

}
