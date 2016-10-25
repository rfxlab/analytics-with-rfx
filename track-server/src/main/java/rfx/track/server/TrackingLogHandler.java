package rfx.track.server;

import org.vertx.java.core.http.HttpServerRequest;

import rfx.core.util.StringPool;
import server.http.handler.BaseHttpHandler;
import server.http.util.HttpTrackingUtil;
import server.http.util.KafkaLogHandlerUtil;

public class TrackingLogHandler implements BaseHttpHandler {
		
	public static final String REDIRECT_PREFIX = "/r/";
	
	
	private static final String PONG = "PONG";
	private static final String DATA = "data";
	private static final String FAVICON_ICO = "favicon.ico";
	private static final String LOG_DATA = "pv";
	private static final String PING = "ping";


	@Override
	public void handle(HttpServerRequest req) {
		String uri;
		if(req.uri().startsWith("/")){
			uri = req.uri().substring(1);	
		} else {
			uri = req.uri();
		}
		
		System.out.println("URI: " + uri);
		
		//common
		if(uri.startsWith(LOG_DATA)){
			KafkaLogHandlerUtil.logAndResponseImage1px(req, "_pageview");;
		} 
		else if (uri.equalsIgnoreCase(FAVICON_ICO)) {
			HttpTrackingUtil.trackingResponse(req);
		}
		else if (uri.equalsIgnoreCase(PING)) {
			req.response().end(PONG);
		}		
		else {
			req.response().end("Not handler found for uri:"+uri);
		}
	}

	@Override
	public String getPathKey() {
		return StringPool.STAR;
	}

}
