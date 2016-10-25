package pageview.util;

public class FptPlayPlacementUtil {

	public static final int FPT_PLAY_MOBILE_DEV_TEST = 333;
	
	public static final int FPT_PLAY_LIVE_STREAM_IOS = 301;	
	public static final int FPT_PLAY_LIVE_STREAM_ANDROID_BOX = 303;
	public static final int FPT_PLAY_LIVE_STREAM_ANDROID_APP = 305;
	public static final int FPT_PLAY_LIVE_STREAM_ANDROID_SMART_TV = 307;
	
	public static final int FPT_PLAY_VOD_IOS = 302;	
	public static final int FPT_PLAY_VOD_ANDROID_BOX = 304;
	public static final int FPT_PLAY_VOD_ANDROID_APP = 306;	
	public static final int FPT_PLAY_VOD_ANDROID_SMART_TV = 308;
	
	public static final int FPT_PLAY_SMARTTV_LIVE_TV = 201;
	public static final int FPT_PLAY_SMART_TV_VOD = 202;	
	
	public static final int FPT_PLAY_VOD = 102;
	public static final int FPT_PLAY_LIVE_TV = 101;
    
    public static boolean isFptPlay(int placementId){
    	//Web Live TV
    	if(placementId == FPT_PLAY_LIVE_TV){			
			return true;
		}
		
		//WEB VOD
		else if(placementId == FPT_PLAY_VOD ){
			return true;
		}	
		
		
		//SMARTTV Placements
		else if(placementId == FPT_PLAY_SMARTTV_LIVE_TV ){
			//SmartTV Live TV
			return true;
		}
		
		else if(placementId == FPT_PLAY_SMART_TV_VOD ){
			//SmartTV VOD				
			return true;
		}
    	
		//Mobile App VOD
		else if(placementId == FPT_PLAY_VOD_IOS || placementId == FPT_PLAY_VOD_ANDROID_APP || placementId == FPT_PLAY_VOD_ANDROID_BOX){
			return true;
		}
    	
		//Mobile App Test
		else if(placementId == FPT_PLAY_MOBILE_DEV_TEST ){
			//FIXME		
			return true;
		}		
    	
		return false;
    }
}
