package chav1961.purelibnavigator;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.Closeable;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class AbstractTray implements Closeable {
	protected final String		host;
	protected final int			port;
	
	private final SystemTray	tray;
	private final Image			imageOn, imageOff;
	private final TrayIcon		icon;
	private boolean				enabled = false;
	
	protected AbstractTray(final String host, final int port) throws IOException {
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("Host can't be null or empty"); 
		}
		else if (port < 1024 || port > 63353) {
			throw new IllegalArgumentException("Port [] must be in range 1024..65535"); 
		}
		else if (!SystemTray.isSupported()) {
			throw new IllegalStateException("System tray is not supported");
		}
		else {
			this.host = host;
			this.port = port;
			this.tray = SystemTray.getSystemTray();
			this.imageOn = ImageIO.read(AbstractTray.class.getResource("enabled.png"));
			this.imageOff = ImageIO.read(AbstractTray.class.getResource("disabled.png"));
			this.icon = new TrayIcon(imageOff);
			this.icon.setImageAutoSize(true);
			
			final PopupMenu	popup = new PopupMenu();
			final MenuItem	copyT1 = new MenuItem("Copy t1");
			final MenuItem	copyAmount = new MenuItem("Copy amount");
			final MenuItem	exit = new MenuItem("exit");
			
			popup.add(copyT1);
			copyT1.addActionListener((e)->copyT1());
			popup.add(copyAmount);
			copyAmount.addActionListener((e)->copyAmount());
			popup.add(exit);
			exit.addActionListener((e)->{
				try{exit();
				} catch (IOException e1) {
				}
			});
			
			icon.setPopupMenu(popup);
			try{tray.add(icon);
			} catch (AWTException e) {
				throw new IOException(e.getLocalizedMessage(),e); 
			}
		}
	}
	
	protected void setEnabled(final boolean enabled) {
		this.enabled = enabled;
		icon.setImage(this.enabled ? imageOn : imageOff);
	}
	
	protected boolean isEnabled() {
		return enabled;
	}
	
	protected void setTooltip(final String message, final Object... parameters) {
		if (parameters != null && parameters.length > 0) {
			icon.setToolTip(String.format(message, parameters));
		}
		else {
			icon.setToolTip(message);
		}
	}

	protected void clearTooltip() {
		icon.setToolTip(null);
	}
	
	protected abstract void copyT1();

	protected abstract void copyAmount();
	
	protected abstract void exit() throws IOException;
	
	@Override
	public void close() throws IOException {
		tray.remove(icon);
	}
}
