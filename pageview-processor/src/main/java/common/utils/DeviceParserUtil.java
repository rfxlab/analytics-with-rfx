package common.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import rfx.core.stream.util.ua.Client;
import rfx.core.stream.util.ua.Parser;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class DeviceParserUtil {

	private static final String SMART_TV_LG = "SmartTV-LG";
	private static final String SMART_TV_SAMSUNG = "SmartTV-Samsung";
	private static final String SMART_TV_SONY = "SmartTV-Sony";
	public static final String DEVICE_TYPE_PC = "General_Desktop";
	public static final String DEVICE_TYPE_MOBILE_WEB = "General_Mobile";
	public static final String DEVICE_TYPE_TABLET = "General_Tablet";
	public static final String DEVICE_ANDROID = "Android";

	public final static int PC = 1;
	public final static int MOBILE_WEB = 2;
	public final static int TABLET = 3;
	public final static int NATIVE_APP = 4;
	public final static int SMART_TV = 5;
	
	
	
	public static int getPlatformId(Client client, boolean isNativeApp){
		if(isNativeApp){
			return NATIVE_APP;
		}
		return getDeviceType(client);
	}
	
	public static int getDeviceType(Client client){
		String p = client.device.deviceType();
		if(DEVICE_TYPE_PC.equals(p)){
			return PC;
		} else if(DEVICE_TYPE_MOBILE_WEB.equals(p)){
			return MOBILE_WEB;
		} else if(DEVICE_TYPE_TABLET.equals(p)){
			return TABLET;
		}
		return PC;
	}
	
	public static final class DeviceInfo {
		public int deviceType;
		public String deviceName;
    	public String deviceOs;
    	
		public DeviceInfo(int deviceType, String deviceName, String deviceOs) {
			super();
			this.deviceType = deviceType;
			this.deviceName = deviceName;
			this.deviceOs = deviceOs;
		}    	
	}
	
	public static final class DeviceUserContext {
		public String useragent;
		public int placementId;
		
		public DeviceUserContext(String useragent, int placementId) {
			super();
			this.useragent = useragent;
			this.placementId = placementId;
		}		
		@Override
		public int hashCode() {			
			return (useragent + String.valueOf(placementId)).hashCode();
		}
	}
	
	static LoadingCache<DeviceUserContext, DeviceInfo> deviceInfoCache = CacheBuilder.newBuilder()
			.maximumSize(5000).expireAfterWrite(1, TimeUnit.SECONDS)
			.build(new CacheLoader<DeviceUserContext, DeviceInfo>() {
				public DeviceInfo load(DeviceUserContext deviceUserContext) {					
					return parse(deviceUserContext);
				}
			});
	
	public static DeviceInfo parse(DeviceUserContext deviceUserContext) {
		//System.out.println(" miss cache, parsing ...");
		int placementId = deviceUserContext.placementId;
		String useragent = deviceUserContext.useragent;
		Client uaClient = Parser.load().parse(useragent);
		int deviceType = PC;
    	String deviceName = "PC";
    	String deviceOs = uaClient.os.family;    	
		if(placementId  == 202 || placementId == 201){
    		if("Linux".equalsIgnoreCase(deviceOs)){
    			deviceType = SMART_TV;			    			
				if(useragent.contains("Sony")){
    				deviceName = SMART_TV_SONY;
    			}
    			else if(useragent.contains("Tizen")){
    				deviceName = SMART_TV_SAMSUNG;	
    			} 
    			else if(useragent.contains("Web0S")){
    				deviceName = SMART_TV_LG;
    			}			    		}
    	} else {
    		deviceType = getDeviceType(uaClient);
    		deviceName = uaClient.device.family.split(" ")[0];
    	}
    	return new DeviceInfo(deviceType, deviceName, deviceOs);
	}
	
	public static DeviceInfo parseWithCache(String useragent, int placementId) throws ExecutionException{
		DeviceUserContext k = new DeviceUserContext(useragent, placementId);
		DeviceInfo v = deviceInfoCache.get(k);
//		if(v == null){
//			v = parse(k);
//			deviceInfoCache.put(k, v);
//		}
		return v;
	}
}
