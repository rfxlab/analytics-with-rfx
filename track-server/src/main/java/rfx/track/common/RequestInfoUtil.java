package rfx.track.common;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map.Entry;

import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;

import rfx.core.util.StringUtil;

public class RequestInfoUtil {
	
	static final String unknown = "unknown" ;
	public static String getRemoteIP(HttpServerRequest request) {
		String ipAddress = request.headers().get("X-Forwarded-For");		
		if ( ! StringUtil.isNullOrEmpty(ipAddress) && ! unknown.equalsIgnoreCase(ipAddress)) {			
			//LogUtil.dumpToFileIpLog(ipAddress);
			String[] toks = ipAddress.split(",");
			int len = toks.length;
			if(len > 1){
				ipAddress = toks[len-1];
			} else {				
				return ipAddress;
			}
		} else {		
			ipAddress = getIpAsString(request.remoteAddress());
		}		
		return ipAddress;
	}
	
	public static String getIpAsString(SocketAddress address) {
		try {
			if(address instanceof InetSocketAddress){
				return ((InetSocketAddress)address).getAddress().getHostAddress();
			}
			return address.toString().split("/")[1].split(":")[0];
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "0.0.0.0";
	}

	public static String getRequestInfo(HttpServerRequest request){
		StringBuilder reqInfo = new StringBuilder();
		
    	String remoteAddress = request.remoteAddress().toString();		
	
		MultiMap headers = request.headers();
		
		
		reqInfo.append(" <br> IP remoteAddress: ").append(remoteAddress);
		for (Entry<String, String> header : headers) {
			reqInfo.append("<br> ").append(header.getKey()).append(" = ").append(header.getValue());	
		}
		
		return reqInfo.toString();
		
	}
}
