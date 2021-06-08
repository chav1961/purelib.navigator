package chav1961.purelibnavigator;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;

public class AWSTest {

	public static void main(String[] args) throws IOException {
        System.setProperty("javax.net.debug", "all");
//        System.setProperty("http.proxyHost", "http://proxy.compassplus.ru");
//        System.setProperty("http.proxyPort", "3128");
//        System.setProperty("http.nonProxyHosts", "*.compassplus.ru;*.compassplus.com;compassplus.ru;compassplus.com;10.*;virtual;localhost;*.radixware.org;radixware.org;*.maven.apache.org;*.amazonaws.com");
//        System.setProperty("https.nonProxyHosts", "*.compassplus.ru;*.compassplus.com;compassplus.ru;compassplus.com;10.*;virtual;localhost;*.radixware.org;radixware.org;*.maven.apache.org;*.amazonaws.com");
        
		final URL url = new URL("https://radixware.s3.eu-central-1.amazonaws.com/org.radixware-4-2.1.34.10-install.zip");
//		final URL url = new URL("https://radixware.org/downloads/manager.zip");
		final Proxy					p = new Proxy(Type.HTTP,new InetSocketAddress("http://proxy.compassplus.ru", 3128));
		final HttpsURLConnection	conn = (HttpsURLConnection) url.openConnection();
	
		System.err.println("Allow? "+conn.getAllowUserInteraction());
//		conn.setAllowUserInteraction(true);
		conn.setInstanceFollowRedirects(false);
		try{conn.connect();
			for (Certificate item : conn.getServerCertificates()) {
				System.err.println("Cert: "+item.getType());
			}
			long	total = 0;
			try(final InputStream	is = conn.getInputStream()) {
				byte[]	buffer = new byte[8192];
				int		len;
				
				while ((len = is.read(buffer)) > 0) {
					total += len;
				}
			}
			System.err.println("Total: "+total);
		} finally {
			conn.disconnect();
		}
		System.err.println("----------------------- end");
	}

}
