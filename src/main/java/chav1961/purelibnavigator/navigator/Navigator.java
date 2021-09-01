package chav1961.purelibnavigator.navigator;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.sun.net.httpserver.HttpServer;

import chav1961.purelib.basic.PureLibSettings;

public class Navigator {
	public static final String	HELP_CAPTION = "Pure Library Help system";
	public static final String	HELP_SERVER_STARTED = "Pure Library Help server started";
	public static final String	HELP_ABOUT = "Pure Library Help system\nVersion="+PureLibSettings.CURRENT_VERSION+"\nBuild date: "+new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis()));
	public static final String	HELP_DESKTOP_NOT_SUPPORTED = "Java doesn't support desktop functionality on your computer";
	public static final String	HELP_ERROR_STARTING_BROWSER = "I/O error starting browser: ";
	public static final String	HELP_TOOLTIP = "Pure Library Help system:\n - double click: start browser\n - right mouse button: context menu";
	
	public static void main(final String[] args) {
		if (!SystemTray.isSupported()) {
            showErrorAndExit(128, "Java doesn't support system tray on your computer");
        }
		else {
			final SystemTray	tray = SystemTray.getSystemTray();
			final TrayIcon		trayIcon;
			
			try{final int				freePort;
				final CountDownLatch	latch = new CountDownLatch(1); 
				
				try(final ServerSocket	ss = new ServerSocket(0)) {
					freePort = ss.getLocalPort();
				}

				final PopupMenu popup = new PopupMenu();
				final MenuItem 	startItem = new MenuItem("Start browser");
				final MenuItem 	exitItem = new MenuItem("Exit application");
				final MenuItem 	aboutItem = new MenuItem("About");
			       
		        popup.add(startItem);
		        popup.add(exitItem);
		        popup.addSeparator();
		        popup.add(aboutItem);

				trayIcon = new TrayIcon(ImageIO.read(Navigator.class.getResourceAsStream("avatar.jpg")));
		        
		        startItem.addActionListener((e)->startBrowser(freePort, trayIcon));
		        exitItem.addActionListener((e)->latch.countDown());
		        aboutItem.addActionListener((e)->trayIcon.displayMessage(HELP_CAPTION, HELP_ABOUT, TrayIcon.MessageType.NONE));
			       
				trayIcon.setPopupMenu(popup);
				trayIcon.setImageAutoSize(true);
				trayIcon.addActionListener((e)->startBrowser(freePort, trayIcon));
				trayIcon.setToolTip(HELP_TOOLTIP);
				
		        tray.add(trayIcon);
		        
				final HttpServer 	server = HttpServer.create(new InetSocketAddress(freePort), 0);
		        
				server.createContext("/", new NavigatorHandler());
		        server.setExecutor(null);
		        server.start();
		        trayIcon.displayMessage(HELP_CAPTION, HELP_SERVER_STARTED, TrayIcon.MessageType.INFO);
		        latch.await();
		        server.stop(0);
		        tray.remove(trayIcon);
			} catch (IOException | InterruptedException | AWTException exc) {
				showErrorAndExit(128, exc.getLocalizedMessage());
			}
		}
	}
	
	private static void startBrowser(final int port, final TrayIcon icon) {
		if (Desktop.isDesktopSupported()) {
			try{Desktop.getDesktop().browse(URI.create("http://localhost:"+port+"/index.html"));
			} catch (IOException exc) {
				icon.displayMessage(HELP_CAPTION, HELP_ERROR_STARTING_BROWSER+exc.getLocalizedMessage(), TrayIcon.MessageType.ERROR);
			}
		}
		else {
			icon.displayMessage(HELP_CAPTION, HELP_DESKTOP_NOT_SUPPORTED, TrayIcon.MessageType.WARNING);
		}
	}

	private static void showErrorAndExit(final int exitCode, final String error) {
		JOptionPane.showMessageDialog(null, error);
		System.exit(exitCode);
	}
}
