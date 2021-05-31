package chav1961.purelibnavigator;

import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.Properties;

public class ClientTray extends AbstractTray {
	private static final String		FORMAT = "";
	private static final long		TIMER_DELAY = 60000;

	private final Thread			current;
	private final Properties		props = new Properties();
	
	protected ClientTray(final String host, final int port) throws IOException {
		super(host, port);
		this.current = Thread.currentThread();
	}

	public PopupMenu buildPopupMenu() {
		return null;
	}
	
	public void listen() {
		try{while (!Thread.interrupted()) {
				try{new ClientConnector(host, port).poll(props);
					setTooltip(FORMAT, props.getProperty(DeviceConnector.PROP_T1), props.getProperty(DeviceConnector.PROP_T2), props.getProperty(DeviceConnector.PROP_T3),
										props.getProperty(DeviceConnector.PROP_T4), props.getProperty(DeviceConnector.PROP_AMOUNT), props.getProperty(DeviceConnector.PROP_LIGHT));
					setEnabled(true);
				} catch (IOException e) {
					clearTooltip();
					setEnabled(false);
				}
				Thread.sleep(TIMER_DELAY);
			}
		} catch (InterruptedException e) {
		}
	}
	
	@Override
	protected void exit() throws IOException {
		current.interrupt();
	}

	@Override
	protected void copyT1() {
		final StringSelection 	ss = new StringSelection(props.getProperty(DeviceConnector.PROP_T1));
		final Clipboard 		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		clipboard.setContents(ss, null);		
	}

	@Override
	protected void copyAmount() {
		final StringSelection 	ss = new StringSelection(props.getProperty(DeviceConnector.PROP_AMOUNT));
		final Clipboard 		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		clipboard.setContents(ss, null);		
	}
}
