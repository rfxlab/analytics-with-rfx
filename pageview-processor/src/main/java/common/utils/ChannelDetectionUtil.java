package common.utils;


import rfx.core.util.StringPool;



public class ChannelDetectionUtil {
	final public static int INVALID_URL = -100;
	final public static int NO_MATCH = -101;
	final public static int NO_WEBSITE = -102;
	final public static int DEFAULT_CATEGORY_ID = 642;
	
	
	public static int getId(int sideId, String url){
		return DEFAULT_CATEGORY_ID;
	}
	
	public static String normalizeUrl(String url){
		//TODO now only check matching short url, so don't care about RFC standard
		url =  url.toLowerCase();
		if(url.startsWith("http") && url.indexOf("://") > 0 && url.indexOf(".") > 0){
			url = url.replaceAll("^(^(http|https)://)|www.", "");
		} else {
			return StringPool.BLANK;
		}
		return url;
	}
	
	public static String normalizeShortUrl(String url){
		//TODO now only check matching short url, so don't care about RFC standard
		url =  url.toLowerCase();
		url = url.replaceAll("^(^(http|https)://)|www.", "");  
	    url = url.replaceAll("//", "");
	    int endIndex = url.lastIndexOf("/");	    
	    if(endIndex == (url.length()-1)){
	    	url = url.substring(0, endIndex);
	    }
		return url;
	}
		

	public static String getHost(String url){	   
	    int doubleslash = url.indexOf("//");
	    if(doubleslash == -1)
	        doubleslash = 0;
	    else
	        doubleslash += 2;
	    int end = url.indexOf('/', doubleslash);
	    end = end >= 0 ? end : url.length();
	    return url.substring(doubleslash, end);
	}
	
	public static String getBaseDomain(String url) {
	    String host = getHost(url);
	    int startIndex = 0;
	    int nextIndex = host.indexOf('.');
	    int lastIndex = host.lastIndexOf('.');
	    while (nextIndex < lastIndex) {
	        startIndex = nextIndex + 1;
	        nextIndex = host.indexOf('.', startIndex);
	    }
	    if (startIndex > 0) {
	        return host.substring(startIndex);
	    } else {
	        return host;
	    }
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//String url = "http://xem24.com/Phim-hay/detail/899-Ke-Chi-diem-(2013)Snitch.html#ep1-tap-1";
		String url = "http://vnexpress.net/bbba";		
		int shorlId = ChannelDetectionUtil.getId(315, url);
		System.out.println("shorlId:"+shorlId );
		String normalizeUrl = ChannelDetectionUtil.normalizeUrl(url);
		System.out.println(normalizeUrl);		
	}

}
