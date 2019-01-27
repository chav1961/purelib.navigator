package chav1961.purelib.navigator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;

import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.XMLDescribedApplication;

/**
 * <p>This class is an application class for Pure Library Navigator.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class Application {
	public static final String	HELP_USING = "Use java -jar purelib.navigator.jar {-http <port>|[<-swing>]} {[-local]|-external} [-shutdown] [-lang {ru|en}]";
	public static final String	UNKNOWN_ARGUMENT = "Unknown command line argument [%1$s]";
	public static final String	ILLEGAL_HTTP_PORT = "Illegal HTTP port [%1$s]: need be any valid number in the range 1..65535";
	public static final String	HTTP_PORT_MISSING = "Mandatory HTTP port is not typed for '%1$s' command line argument";
	public static final String	LANGUAGE_MISSING = "Mandatory language name is not typed for '-lang' command line argument";
	public static final String	MUTUALLY_EXCLUSIVE_PARAMETERS = "Mutually exclusive parameters %1$s and %2$s were typed";
	public static final String	SHUTDOWN_REQUIRES_HTTP = "Using %1$s parameter requires to type %2$s also";
	
	public static final String	KEY_HTTP = "-http";
	public static final String	KEY_SWING = "-swing";
	public static final String	KEY_LOCAL = "-local";
	public static final String	KEY_EXTERNAL = "-external";
	public static final String	KEY_SHUTDOWN = "-shutdown";
	public static final String	KEY_LANG = "-lang";
	
	public static final String	APPLICATION_XML = "application.xml";

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
		// TODO Auto-generated method stub
		if (args.length == 0) {
			printAndExit(1);
		}
		else {
			boolean		wasHttp = false, wasSwing = false, wasLocal = false, wasExternal = false, wasShutdown = false, wasLang = false;
			String		lang = Locale.getDefault().getLanguage().equals(new Locale("ru").getLanguage()) ? new Locale("ru").getLanguage() : new Locale("en").getLanguage();
			int			httpPort = 0;
			
			for (int index = 0; index < args.length; index++) {
				switch (args[index]) {
					case KEY_HTTP		:
						wasHttp = true;
						if (index < args.length-1) {
							try{httpPort = Integer.valueOf(args[++index]);
								if (httpPort < 1 || httpPort >= 65536) {
									System.err.println(String.format(ILLEGAL_HTTP_PORT,args[index]));
									printAndExit(128);
								}
							} catch (NumberFormatException exc) {
								System.err.println(String.format(ILLEGAL_HTTP_PORT,args[index]));
								printAndExit(128);
							}
						}
						else {
							System.err.println(String.format(HTTP_PORT_MISSING,KEY_HTTP));
							printAndExit(128);
						}
						break;
					case KEY_SWING		:
						wasSwing = true;
						break;
					case KEY_LOCAL		:
						wasLocal = true;
						break;
					case KEY_EXTERNAL	:
						wasExternal = true;
						break;
					case KEY_SHUTDOWN	:
						wasShutdown = true;
						break;
					case KEY_LANG		:
						wasLang = true;
						if (index < args.length-1) {
							lang = args[++index];
							if (lang.equals("ru")) {
								lang = new Locale("ru").getLanguage();
							}
							else {
								lang = new Locale("en").getLanguage();
							}
						}
						else {
							System.err.println(LANGUAGE_MISSING);
							printAndExit(128);
						}
						break;
					default :
						System.err.println(String.format(UNKNOWN_ARGUMENT,args[index]));
						printAndExit(128);
						break;
				}
			}
			if (wasHttp && wasSwing) {
				System.err.println(String.format(MUTUALLY_EXCLUSIVE_PARAMETERS,KEY_HTTP,KEY_SWING));
				printAndExit(128);
			}
			else if (wasLocal && wasExternal) {
				System.err.println(String.format(MUTUALLY_EXCLUSIVE_PARAMETERS,KEY_LOCAL,KEY_EXTERNAL));
				printAndExit(128);
			}
			else if (wasShutdown && !wasHttp) {
				System.err.println(String.format(SHUTDOWN_REQUIRES_HTTP,KEY_SHUTDOWN,KEY_HTTP));
				printAndExit(128);
			}
			else if (wasHttp && wasShutdown) {
				System.exit(stopServer(httpPort));
			}
			else if (wasHttp && !wasShutdown) {
				startServer(httpPort,lang);
			}
			else {
				startGUI(lang);
			}
		}
	}
	
	private static int startGUI(final String lang) {
		Locale.setDefault(new Locale(lang));
		
		try{final Localizer						purelibLocalizer = new PureLibLocalizer();
			try(final InputStream				is = Application.class.getResourceAsStream(APPLICATION_XML);
				final LoggerFacade				logger = new SystemErrLoggerFacade()) {
				final XMLDescribedApplication	xda = new XMLDescribedApplication(is,logger);
				
				new GUIApplication(xda,purelibLocalizer.push(xda.getLocalizer())).setVisible(true);
			}
			return 0;
		} catch (EnvironmentException | IOException exc) {
			exc.printStackTrace();
			return 129;
		}
	}


	private static int startServer(final int httpPort, final String lang) {
		// TODO Auto-generated method stub
		return 0;
	}


	private static int stopServer(final int httpPort) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static void printAndExit(final int rc) {
		System.err.println(HELP_USING);
		System.exit(1);
	}
}
