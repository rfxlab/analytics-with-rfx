package rfx.track.common;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ip2location.IP2Location;
import com.ip2location.IPResult;

import rfx.core.configs.WorkerConfigs;
import rfx.core.util.StringPool;
import rfx.core.util.StringUtil;
import rfx.track.common.LocationUtil.LocationCacheObj;

public class Ip2LocationUtil {
	
	private static final String VN = "vn";
	private static final String OK = "OK";
	static final IP2Location loc = new IP2Location();
	static {
		loc.IPDatabasePath = WorkerConfigs.load().getCustomConfig("fullIP2LocationPath");
		System.out.println("loaded IPDatabasePath: "+loc.IPDatabasePath);
	}
	
	static LoadingCache<String, IPResult> ipToLocationCache = CacheBuilder.newBuilder()
			.maximumSize(10000000).expireAfterWrite(30, TimeUnit.MINUTES)
			.build(new CacheLoader<String, IPResult>() {
				public IPResult load(String ip) {
					try {
						return loc.IPQuery(ip);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			});
	
	public static String findCountryCodeFromIP2Location(String ip){
		String countryCode = VN;
		try {
			IPResult rec = loc.IPQuery(ip);	
			if (OK.equals(rec.getStatus())) {
				countryCode = rec.getCountryShort().toLowerCase();
				if(countryCode.equals("-")){
					countryCode = VN;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return countryCode;
	}
	
	public static String findCityAndUpdateStats(String ip, String locationFromFtel){
		return findCityAndUpdateStats(ip,null,locationFromFtel);
	}
	
	public static String findCityAndUpdateStats(String ip){
		String city = StringPool.BLANK;
		try {
			IPResult rec = ipToLocationCache.get(ip);
			if (OK.equals(rec.getStatus())) {
				city = rec.getCity();							
			}
			if(city.equals(StringPool.MINUS)){
				//not found any city matched with IP
				city = StringPool.BLANK;
			} 	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return city;
	}
	
	public static String findCityAndUpdateStats(String ip, String updateMetric, String locationFromFtel){
		String city = StringPool.BLANK;
		try {
			IPResult rec = ipToLocationCache.get(ip);
			if (OK.equals(rec.getStatus())) {
				city = rec.getCity();							
			}
			if(city.equals(StringPool.MINUS)){
				//not found any city matched with IP
				city = StringPool.BLANK;
			} else {
				//TODO
//				if(updateMetric != null){
//					RealtimeTrackingUtil.updateAndGetLocationId(DateTimeUtil.currentUnixTimestamp(), rec, updateMetric,locationFromFtel);	
//				}					
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return city;
	}
	
	public static LocationCacheObj find(String ip){
		//System.out.println(ip);
		if(StringUtil.isNotEmpty(ip)){
			String countryCode;
			try {
				//find from Redis first
				LocationCacheObj locObj = LocationUtil.getLocationFromIp(ip);
				countryCode = locObj.getCountryCode();				
				if(! VN.equals(countryCode) ){
					//not in Redis, so find from DB of IP2Location
					countryCode = findCountryCodeFromIP2Location(ip);
					locObj.setCountryCode(countryCode);
				}								
				return locObj;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		String ip = "222.252.34.156";
		//ip = "115.78.234.190";
		
		IPResult rec = loc.IPQuery(ip);
		if (OK.equals(rec.getStatus())) {
			System.out.println(rec.getRegion());
		} else if ("EMPTY_IP_ADDRESS".equals(rec.getStatus())) {
			System.out.println("IP address cannot be blank.");
		} else if ("INVALID_IP_ADDRESS".equals(rec.getStatus())) {
			System.out.println("Invalid IP address.");
		} else if ("MISSING_FILE".equals(rec.getStatus())) {
			System.out.println("Invalid database path.");
		} else {
			System.out.println("Unknown error." + rec.getStatus());
		}
		if (rec.getDelay() == true) {
			System.out.println("@@@@The last query was delayed for 5 seconds because this is an evaluation copy.");
		}
		System.out.println("Java Component: " + rec.getVersion());
		
		System.out.println(findCityAndUpdateStats(ip, null, null));
	}
}
