package rfx.track.common;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;

import rfx.core.util.Utils;

public class NettyHttpClient {

	public static void main(String[] args) {
		// Configure the client.
		String adCode3rd = "/?advideo/3.0/78.109/5798044/0//cc=2;vidAS=pre_roll;vidRT=VAST;vidRTV=2.0.1";
		HttpClient httpClient = VertxFactory.newVertx().createHttpClient();
		httpClient.setHost("adserver.adtech.de");
		httpClient.get(adCode3rd, new Handler<HttpClientResponse>() {

		    @Override
		    public void handle(HttpClientResponse httpClientResponse) {
		    	httpClientResponse.bodyHandler(new Handler<Buffer>() {
		            @Override
		            public void handle(Buffer buffer) {
		                System.out.println("Response (" + buffer.length() + "): ");
		                System.out.println(buffer.getString(0, buffer.length()));
		            }
		        });
		    }
		});
		Utils.sleep(10000);
	}
}
