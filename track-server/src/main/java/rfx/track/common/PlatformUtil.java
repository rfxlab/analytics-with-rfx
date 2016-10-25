package rfx.track.common;



public class PlatformUtil {
	public static int PLATFORM_PC = 1;
	public static int PLATFORM_MOBILE = 2;

	
	
	public static int getPlatformId(String useragent){
		int platformId = PLATFORM_PC;
	    return platformId;
	}
	
	
}
