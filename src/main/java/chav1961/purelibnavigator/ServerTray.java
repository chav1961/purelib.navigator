package chav1961.purelibnavigator;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.Properties;

public class ServerTray extends AbstractTray {
	private final DeviceConnector	dc;
	private final ServerConnector	sc;
	
	protected ServerTray(final int port, final DeviceConnector dc, final ServerConnector sc) throws IOException {
		super("localhost", port);
		this.dc = dc;
		this.sc = sc;
	}

	@Override
	protected void copyT1() {
		final Properties		props = new Properties();
		
		try{dc.poll(props);
			final StringSelection 	ss = new StringSelection(props.getProperty(DeviceConnector.PROP_T1));
			final Clipboard 		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			
			clipboard.setContents(ss, null);		
		} catch (IOException e) {
		}
	}

	@Override
	protected void copyAmount() {
		final Properties		props = new Properties();
		
		try{dc.poll(props);
			final StringSelection 	ss = new StringSelection(props.getProperty(DeviceConnector.PROP_AMOUNT));
			final Clipboard 		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			
			clipboard.setContents(ss, null);		
		} catch (IOException e) {
		}
	}
	
	@Override
	protected void exit() throws IOException {
		sc.close();
	}
}
