package chav1961.purelibnavigator;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class ServerConnector implements Closeable {
	private static final String	QUERY_POLL = "";
	
	private final ServerSocket		ss;
	private final DeviceConnector	conn;
	
	public ServerConnector(final int port, final DeviceConnector conn) throws IOException {
		if (port < 1024 || port > 65536) {
			throw new IllegalArgumentException("Port ["+port+"] must be in range 1024..65535");
		}
		else if (conn == null) {
			throw new NullPointerException("Device connector can't be null");
		}
		else {
			this.ss = new ServerSocket(port);
			this.conn = conn;
		}
	}

	public void listen() throws IOException {
		while (!Thread.interrupted()) {
			final Socket	sock = ss.accept();
			final Thread	t = new Thread(()->process(sock));
			
			t.setDaemon(true);
			t.start();
		}
	}
	
	private void process(final Socket sock) {
		try(final Socket				s = sock;
			final InputStream			is = s.getInputStream();
			final OutputStream			os = s.getOutputStream();
			final Reader				rdr = new InputStreamReader(is);
			final BufferedReader		brdr = new BufferedReader(rdr);
			final ObjectOutputStream	oos = new ObjectOutputStream(os)) {
			
			if (QUERY_POLL.equals(brdr.readLine())) {
				final Properties		props = new Properties();
				
				conn.poll(props);
				oos.writeObject(props);
			}
			else {
				oos.writeObject(null);
			}
			oos.flush();
			oos.reset();
		} catch (IOException e) {
		}
	}

	@Override
	public void close() throws IOException {
		this.ss.close();
	}
}
