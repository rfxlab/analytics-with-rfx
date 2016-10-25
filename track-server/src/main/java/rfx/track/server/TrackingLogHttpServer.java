package rfx.track.server;

import rfx.core.util.StringUtil;

public class TrackingLogHttpServer {
	public static void main(String[] args) throws Exception {
		if(args.length == 0 ){
			args = new String[]{"127.0.0.1","8080"};
		}		
		String host = args[0];
		int port = StringUtil.safeParseInt(args[1]);		
		server.http.LogHttpWorker.startNewInstance(host, port, new TrackingLogHandler());
	}
}