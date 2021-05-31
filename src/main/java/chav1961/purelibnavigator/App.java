package chav1961.purelibnavigator;

import java.io.IOException;

public class App {

	public static void main(String[] args) {
		if (args.length == 0) {
			printUsage();
		}
		else if (args.length == 1) {
			if (checkHostPort(args[0])) {
				try(final ClientTray		tray = new ClientTray(extractHost(args[0]),extractPort(args[0]))) {

					tray.listen();
				} catch (IOException e) {
				}					
			}
			else {
				printParameterError("Illegal host/port");
			}
		}
		else if (args.length == 2) {
			if (checkHostPort(args[0])) {
				if (checkDevice(args[1])) {
					try(final DeviceConnector	dc = new DeviceConnector(args[1]);
						final ServerConnector	sc = new ServerConnector(extractPort(args[0]), dc);
						final ServerTray		tray = new ServerTray(extractPort(args[0]), dc, sc)){

						sc.listen();
					} catch (IOException e) {
					}					
				}
				else {
					printParameterError("Illegal device");
				}
			}
			else {
				printParameterError("Illegal host/port");
			}
		}
		else {
			printUsage();
		}
	}

	private static void printUsage() {
		System.err.println("Use: ");
	}
	
	private static void printParameterError(final String message) {
		System.err.println(message);
		printUsage();
		System.exit(128);
	}
	
	private static boolean checkHostPort(final String hostPort) {
		if (hostPort.contains(":")) {
			final int		index = hostPort.lastIndexOf(':');
			final String	host = hostPort.substring(0,index);
			final String	port = hostPort.substring(index+1);
			
			try{final int	val = Integer.valueOf(port);
			
				return val >= 1024 && val <= 65535;
			} catch (NumberFormatException exc) {
				return false;
			}
		}
		else {
			return false;
		}
	}

	private static boolean checkDevice(final String device) {
		return device.length() > 3;
	}

	private static String extractHost(final String hostPort) {
		final int		index = hostPort.lastIndexOf(':');
		
		return hostPort.substring(0,index);
	}
	
	private static int extractPort(final String hostPort) {
		final int		index = hostPort.lastIndexOf(':');
		final String	port = hostPort.substring(index+1);
		
		return Integer.valueOf(port);
	}
}
