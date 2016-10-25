package rfx.track.common;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;

import com.google.gson.Gson;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import rfx.core.util.StringPool;
import rfx.core.util.StringUtil;
import rfx.track.common.LocationUtil.LocationCacheObj;
import server.http.handler.BaseHttpHandler;

public abstract class BaseHttpLogHandler implements BaseHttpHandler {

	public final static String DEFAULT_PATH = "/";
	
	private static final String _0_0_0_0 = "0.0.0.0";;
	private static final String unknown = "unknown" ;
	public final static int COOKIE_AGE_1_WEEK = 604800; // One week
	public final static int COOKIE_AGE_10_YEAR = 31557600 * 10 ; // 10 years

	protected static final String PONG = "PONG";
	protected static final String FAVICON_ICO = "/favicon.ico";
	protected static final String PING = "/ping";

	protected static final String getKafkaKey(String logUri) {
		return logUri.replaceAll("/", "_");
	}

	public BaseHttpLogHandler() {
		super();
	}	

	@Override
	public String getPathKey() {
		return StringPool.STAR;
	}	
	
	public static String getRemoteIP(HttpServerRequest request) {
		try {
			SocketAddress remoteAddress = request.remoteAddress();
			if(remoteAddress instanceof InetSocketAddress){
				return ((InetSocketAddress)remoteAddress).getAddress().getHostAddress();
			}
			return remoteAddress.toString().split("/")[1].split(":")[0];
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return _0_0_0_0;
	}
	
	public static String getRequestIP(HttpServerRequest request){
		String ipAddress = request.headers().get("X-Forwarded-For");		
		if ( ! StringUtil.isNullOrEmpty(ipAddress) && ! unknown.equalsIgnoreCase(ipAddress)) {
			String[] toks = ipAddress.split(",");
			int len = toks.length;
			if(len > 1){
				ipAddress = toks[len-1];
			} else {				
				return ipAddress;
			}
		} else {		
			ipAddress = getRemoteIP(request);
		}		
		return ipAddress;
	}
	
	public static String location2JSON(String ip, LocationCacheObj locObj){
		Map<String, Map<String,Object>> map = new HashMap<>(1);
		Map<String,Object> locMap = new HashMap<>(4);
		locMap.put("ip", ip);
		locMap.put("country", locObj.getCountryCode());
		locMap.put("zone", locObj.getZone());
		locMap.put("province", locObj.getProvince());
		map.put("loc", locMap);
		return new Gson().toJson(map);
	}
	
	public static Cookie createCookie(String name, String value, String domain,
			String path) {
		Cookie cookie = new DefaultCookie(name, value);
		cookie.setDomain(domain);
		cookie.setPath(path);
		return cookie;
	}
	
	public void setCorsHeaders(MultiMap headers){
		headers.set("Access-Control-Allow-Origin","*");
		headers.set("Access-Control-Allow-Credentials", "true");		
		headers.set("Access-Control-Allow-Methods", "POST, GET");
		headers.set("Access-Control-Allow-Headers", "Content-Type, *");		
	}

}