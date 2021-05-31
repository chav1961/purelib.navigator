package chav1961.purelibnavigator;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceConnector implements Closeable {
	public static final String		PROP_T1 = "";
	public static final String		PROP_T2 = "";
	public static final String		PROP_T3 = "";
	public static final String		PROP_T4 = "";
	public static final String		PROP_AMOUNT = "";
	public static final String		PROP_LIGHT = "";
	public static final String		PROP_VALUE_UNKNOWN = "";
	
	private static final String		QUERY_VER = "";
	private static final String		QUERY_SEL_0 = "";
	private static final String		ANS_VER = "";
	private static final String		QUERY_VER_1 = "";
	private static final String		ANS_VER_1 = "";
	private static final String		QUERY_VER_2 = "";
	private static final String		ANS_VER_2 = "";
	private static final String		QUERY_SEL_1 = "";
	private static final String		QUERY_STATE_1 = "";
	private static final Pattern	STATE_1 = Pattern.compile(".*");		
	private static final String		QUERY_SEL_2 = "";
	private static final String		QUERY_STATE_2 = "";
	private static final Pattern	STATE_2 = Pattern.compile(".*");		
	private static final String		ANS_OK = "";
	
	private final RandomAccessFile	raf;
	private boolean					available = false;
	
	public DeviceConnector(final String deviceId) throws IOException {
		if (deviceId == null || deviceId.isEmpty()) {
			throw new IllegalArgumentException("Device id can't be null or empty");
		}
		else {
			this.raf = new RandomAccessFile(deviceId, "rw");
		}
	}

	public void connect() throws IOException {
		available = validate(ANS_VER,QUERY_VER);
	}
	
	public boolean isAvailable() {
		return available;
	}
	
	public synchronized void poll(final Properties props) throws IOException {
		if (props == null) {
			throw new NullPointerException("Properties to fill cna't be nul");
		}
		else {
			if (validate(ANS_OK,QUERY_SEL_1) && validate(ANS_VER_1,QUERY_VER_1)) {
				final Matcher	m = STATE_1.matcher(query(QUERY_STATE_1));
				
				if (m.find()) {
					props.setProperty(PROP_T1, m.group(1));
					props.setProperty(PROP_T2, m.group(2));
					props.setProperty(PROP_LIGHT, m.group(3));
				}
				else {
					props.setProperty(PROP_T1, PROP_VALUE_UNKNOWN);
					props.setProperty(PROP_T2, PROP_VALUE_UNKNOWN);
					props.setProperty(PROP_LIGHT, PROP_VALUE_UNKNOWN);
				}
			}
			else {
				props.setProperty(PROP_T1, PROP_VALUE_UNKNOWN);
				props.setProperty(PROP_T2, PROP_VALUE_UNKNOWN);
				props.setProperty(PROP_LIGHT, PROP_VALUE_UNKNOWN);
			}
			if (validate(ANS_OK,QUERY_SEL_2) && validate(ANS_VER_2,QUERY_VER_2)) {
				final Matcher	m = STATE_2.matcher(query(QUERY_STATE_2));
				
				if (m.find()) {
					props.setProperty(PROP_T3, m.group(1));
					props.setProperty(PROP_T4, m.group(2));
					props.setProperty(PROP_AMOUNT, m.group(3));
				}
				else {
					props.setProperty(PROP_T3, PROP_VALUE_UNKNOWN);
					props.setProperty(PROP_T4, PROP_VALUE_UNKNOWN);
					props.setProperty(PROP_AMOUNT, PROP_VALUE_UNKNOWN);
				}
			}
			validate(ANS_OK,QUERY_SEL_0);
		}
	}
	
	@Override
	public void close() throws IOException {
		raf.close();
	}
	
	private void writeString(final String message, final Object... parameters) throws IOException {
		if (parameters != null && parameters.length > 0) {
			writeString(String.format(message,parameters));
		}
		else {
			raf.write((message+'\n').getBytes());
		}
	}
	
	private String readMessage() throws IOException {
		final StringBuilder	sb = new StringBuilder();
		char	symbol;
		
		do {sb.append(symbol = (char) raf.read());			
		} while (symbol != '\n');
		return sb.toString();
	}
	
	private String query(final String message, final Object... parameters) throws IOException {
		writeString(message,parameters);
		return readMessage();
	}
	
	private boolean validate(final String answer, final String message, final Object... parameters) throws IOException {
		final Pattern	p = Pattern.compile(answer);
		
		writeString(message,parameters);
		return p.matcher(readMessage()).matches();
	}
}
