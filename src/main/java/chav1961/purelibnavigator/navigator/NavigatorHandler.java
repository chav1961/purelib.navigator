package chav1961.purelibnavigator.navigator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class NavigatorHandler implements HttpHandler {
    @Override
    public void handle(final HttpExchange x) throws IOException {
    	if ("GET".equalsIgnoreCase(x.getRequestMethod())) {
        	final URI			resource = x.getRequestURI();
        	final InputStream	is = this.getClass().getResourceAsStream(resource.getPath());
        	
        	if (is != null) {
        		sendResponse(x, 200, is);
        	}
        	else {
        		sendResponse(x, 404, this.getClass().getResourceAsStream("notFound.html"));
        	}
    	}
    	else {
    		sendResponse(x, 502, this.getClass().getResourceAsStream("illegalRequest.html"));
    	}
    }

    private void sendResponse(final HttpExchange x, final int responseCode, final InputStream content) throws IOException {
		x.sendResponseHeaders(responseCode, 0);
		
		try(final OutputStream 	os = x.getResponseBody()) {
			final byte[]		buffer = new byte[8192];
			int					len;
			
			while ((len = content.read(buffer)) > 0) {
				os.write(buffer, 0, len);
			}
		}
    }
}
