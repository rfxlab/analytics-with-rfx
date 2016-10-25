package common.utils;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;

import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import rfx.core.configs.RedisConfigs;
import rfx.core.configs.WorkerConfigs;
import rfx.core.nosql.jedis.RedisCommand;
import rfx.core.stream.util.HashUtil;
import rfx.core.util.CharPool;
import rfx.core.util.StringUtil;

public class IP2LocationUtil {
	
	final public static ShardedJedisPool jedisLocationDataStats = RedisConfigs.load().get("locationDataStats").getShardedJedisPool();
	private static final String OK = "OK";
	static final IP2Location loc = new IP2Location();
	
	static {
		loc.IPDatabasePath = WorkerConfigs.load().getCustomConfig("fullIP2LocationPath");
		System.out.println("loaded IPDatabasePath: "+loc.IPDatabasePath);
	}
	
	public static IPResult find(String ip){
		try {
			IPResult rec = loc.IPQuery(ip);
			if (OK.equals(rec.getStatus())) {
				return rec;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}	
	
	public static long updateAndGetLocationId(int unixtime, IPResult ipResult){
		final String country = ipResult.getCountryLong();
		final String city = ipResult.getCity();
		final String loc = country + CharPool.POUND + city + CharPool.POUND + ipResult.getLatitude() + CharPool.POUND + ipResult.getLongitude();		
		final long locId = HashUtil.hashUrl128Bit(loc);		
		new RedisCommand<Boolean>(jedisLocationDataStats) {
			@Override
			protected Boolean build() throws JedisException {								
				String keyLoc = "lc:"+locId;
				String rs = jedis.get(keyLoc);							
				if(StringUtil.isEmpty(rs)){
					jedis.set(keyLoc, loc);
				}
				return true;
			}
		}.execute();
		
		return locId;
	}
}
