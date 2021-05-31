package chav1961.purelibnavigator;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Properties;

public class ClientConnector {
	private static final String	QUERY_POLL = "";
	
	private final String		host;
	private final int			port;
	
	public ClientConnector(final String host, final int port) throws IOException {
		if (host == null || host.isEmpty()) {
			throw new NullPointerException("Host can't be null or empty");
		}
		else if (port < 1024 || port > 65535) {
			throw new IllegalArgumentException("Port value ["+port+"] must be in range 1024..65535");
		}
		else {
			this.host = host;
			this.port = port;
			try(final Socket	sock = new Socket(host, port)) {
				
			} catch (SocketTimeoutException e) {
			} catch (UnknownHostException e) {
				throw new IOException("Unknown host name ["+host+"]"); 
			}
		}
	}
	
	public boolean poll(final Properties props) throws IOException {
		try(final Socket			sock = new Socket(host, port);
			final InputStream		is = sock.getInputStream();
			final OutputStream		os = sock.getOutputStream();
			final ObjectInputStream	ois = new ObjectInputStream(is);
			final PrintWriter		wr = new PrintWriter(os)) {
			
			wr.println(QUERY_POLL);
			wr.flush();
			final Properties		answer = (Properties) ois.readObject();
			
			props.clear();
			props.putAll(answer);
			return true;
		} catch (SocketTimeoutException e) {
			return false;
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}
}
