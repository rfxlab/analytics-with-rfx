package rfx.track.common;

import java.util.Date;
import java.util.Map;

import com.google.gson.Gson;
import com.ip2location.IPResult;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import rfx.core.configs.RedisConfigs;
import rfx.core.nosql.jedis.RedisCommand;
import rfx.core.stream.util.HashUtil;
import rfx.core.util.CharPool;
import rfx.core.util.DateTimeUtil;
import rfx.core.util.StringUtil;


/**
 * @author trieu
 *
 */
public class RealtimeTrackingUtil {
	
	private static final String EVUT = "evut-";
	private static final String EVENT_STATS = "event-stats";
	private static final String SUMMARY = "summary";
	private static final String PREFIX_MONITOR = "m:";
	private static final String PREFIX_PAGEVIEW = "pv:";

	public static final String DATE_HOUR_MINUTE_FORMAT_PATTERN = "yyyy-MM-dd-HH-mm";
	public static final String DATE_HOUR_MINUTE_SECOND_FORMAT_PATTERN = "yyyy-MM-dd-HH-mm-ss";

	public static final int ONE_DAY = 86400;
	public static final int AFTER_3_DAYS = ONE_DAY * 3;
	public static final int AFTER_7_DAYS = ONE_DAY * 7;
	public static final int AFTER_15_DAYS = ONE_DAY * 15;
	public static final int AFTER_30_DAYS = ONE_DAY * 30;
	public static final int AFTER_60_DAYS = ONE_DAY * 60;	
		
	static ShardedJedisPool jedisPool = RedisConfigs.load().get("realtimeDataStats").getShardedJedisPool();
	static ShardedJedisPool jedisLocationDataStats = RedisConfigs.load().get("locationDataStats").getShardedJedisPool();
	
	
	public static boolean updateEvent(int unixtime, final String event){
		return updateEvent(unixtime, event, false);
	}
	
	public static boolean updateEvent(String prefix,int unixtime, final String event, boolean withSummary){
		return updateEvent(prefix, unixtime, new String[] {event}, withSummary);
	}
	public static boolean updateEvent(int unixtime, final String event, boolean withSummary){
		return updateEvent(PREFIX_MONITOR, unixtime, new String[] {event}, withSummary);
	}
	public static boolean updateEvent(int unixtime, final String[] events, boolean withSummary){
		return updateEvent(PREFIX_MONITOR, unixtime, events, withSummary);
	}
	
	public static boolean updateEvent(String prefix, int unixtime, final String[] events, boolean withSummary){
		boolean commited = false;
		try {
			Date date = new Date(unixtime*1000L);
			final String dateStr = DateTimeUtil.formatDate(date ,DateTimeUtil.DATE_FORMAT_PATTERN);
			final String dateHourStr = DateTimeUtil.formatDate(date ,DateTimeUtil.DATE_HOUR_FORMAT_PATTERN);				
			RedisCommand<Boolean> cmd = new RedisCommand<Boolean>(jedisPool) {
				@Override
				protected Boolean build() throws JedisException {					
					
					String keyD = prefix+dateStr;
					String keyH = prefix+dateHourStr;
					
					Pipeline p = jedis.pipelined();
					for (String event : events) {
						if(withSummary){
							p.hincrBy(SUMMARY, event , 1L);
						}
						
						//hourly
						p.hincrBy(keyH, event , 1L);
						p.expire(keyH, AFTER_15_DAYS);
						
						//daily
						p.hincrBy(keyD, event , 1L);
						p.expire(keyD, AFTER_60_DAYS);	
					}
					
					p.sync();
					return true;
				}
			};
			if(cmd != null) {
				commited = cmd.execute();	
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return commited;
	}
	
	public static boolean updateEventPageView(final String uuid, final String event, final String hostReferer, long delta){
		boolean commited = false;
		try {
			Date date = new Date();
			final String dateStr = DateTimeUtil.formatDate(date ,DateTimeUtil.DATE_FORMAT_PATTERN);
			final String dateHourStr = DateTimeUtil.formatDate(date ,DateTimeUtil.DATE_HOUR_FORMAT_PATTERN);	
			
			RedisCommand<Boolean> cmd = new RedisCommand<Boolean>(jedisPool) {
				@Override
				protected Boolean build() throws JedisException {
					Pipeline p = jedis.pipelined();
					
					p.hincrBy(EVENT_STATS, event , delta);
					p.pfadd(EVUT+event, uuid);
					
					String keyH = PREFIX_PAGEVIEW+dateHourStr;
					p.hincrBy(keyH, event , delta);
					p.expire(keyH, AFTER_15_DAYS);
					
					String keyD = PREFIX_PAGEVIEW+dateStr;
					p.hincrBy(keyD, event , delta);
					p.expire(keyD, AFTER_60_DAYS);
									
					if(StringUtil.isNotEmpty(uuid)){
						String keyHU = PREFIX_PAGEVIEW+dateHourStr + ":u";												
						p.pfadd(keyHU, uuid);
						p.expire(keyHU, AFTER_3_DAYS);	
						
						String keyDU = PREFIX_PAGEVIEW+dateStr + ":u";
						p.pfadd(keyDU, uuid);
						p.expire(keyDU, AFTER_15_DAYS);	
					}	
					
					if(StringUtil.isNotEmpty(event)){
						String keyHUT = PREFIX_PAGEVIEW+dateHourStr + ":u:"+event+":"+hostReferer;											
						p.pfadd(keyHUT, uuid);
						p.expire(keyHUT, AFTER_3_DAYS);	
						
						String keyDUT = PREFIX_PAGEVIEW+dateStr + ":u:"+event+":"+hostReferer;
						p.pfadd(keyDUT, uuid);
						p.expire(keyDUT, AFTER_15_DAYS);	
					}	
					
					p.sync();
					return true;
				}
			};
			if(cmd != null) {
				commited = cmd.execute();	
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return commited;
	}
	
	public static boolean updatePageViewEvent(final String host, final String uuid, final String tag){
		boolean commited = false;
		try {
			Date date = new Date();
			final String dateStr = DateTimeUtil.formatDate(date ,DateTimeUtil.DATE_FORMAT_PATTERN);
			final String dateHourStr = DateTimeUtil.formatDate(date ,DateTimeUtil.DATE_HOUR_FORMAT_PATTERN);				
			RedisCommand<Boolean> cmd = new RedisCommand<Boolean>(jedisPool) {
				@Override
				protected Boolean build() throws JedisException {
					Pipeline p = jedis.pipelined();
										
					String keyH = PREFIX_PAGEVIEW+dateHourStr;
					p.hincrBy(keyH, host , 1L);
					p.expire(keyH, AFTER_15_DAYS);
					
					String keyD = PREFIX_PAGEVIEW+dateStr;
					p.hincrBy(keyD, host , 1L);
					p.expire(keyD, AFTER_60_DAYS);
									
					if(StringUtil.isNotEmpty(uuid)){
						String keyHU = PREFIX_PAGEVIEW+dateHourStr + ":u";												
						p.pfadd(keyHU, uuid);
						p.expire(keyHU, AFTER_3_DAYS);	
						
						String keyDU = PREFIX_PAGEVIEW+dateStr + ":u";
						p.pfadd(keyDU, uuid);
						p.expire(keyDU, AFTER_15_DAYS);	
					}	
					
					if(StringUtil.isNotEmpty(tag)){
						String keyHUT = PREFIX_PAGEVIEW+dateHourStr + ":u:"+host+":"+tag;											
						p.pfadd(keyHUT, uuid);
						p.expire(keyHUT, AFTER_3_DAYS);	
						
						String keyDUT = PREFIX_PAGEVIEW+dateStr + ":u:"+host+":"+tag;
						p.pfadd(keyDUT, uuid);
						p.expire(keyDUT, AFTER_15_DAYS);	
					}	
					
					p.sync();
					return true;
				}
			};
			if(cmd != null) {
				commited = cmd.execute();	
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return commited;
	}
	
		
	public static boolean updateKafkaLogEvent(int unixtime, final String event){
		return updateEvent(unixtime, "kk:"+event);
	}
	
	public static String getAllKafkaLogEvents(final String pkey){
		final String key;
		if(pkey == null){
			key = PREFIX_MONITOR+DateTimeUtil.formatDate(new Date() ,DateTimeUtil.DATE_HOUR_FORMAT_PATTERN);
		} else {
			key = pkey;
		}
		if(key.startsWith(PREFIX_MONITOR)){
			String s = new RedisCommand<String>(jedisPool) {
				@Override
				protected String build() throws JedisException {
					Map<String, String> map = jedis.hgetAll(key);					
					return new Gson().toJson(map);
				}
			}.execute();
			return s;
		}
		return "No data";
	}
	
	public static long updateAndGetLocationId(int unixtime, IPResult ipResult, String metric, String locationFromFtel){
		String country = ipResult.getCountryLong();
		String city = ipResult.getCity();
		String region = ipResult.getRegion().toLowerCase();
		Date date = new Date(unixtime*1000L);
		String dateStr = DateTimeUtil.formatDate(date , DateTimeUtil.DATE_FORMAT_PATTERN);
		String dateHourStr = DateTimeUtil.formatDate(date , DateTimeUtil.DATE_HOUR_FORMAT_PATTERN);
		
		StringBuilder locStr = new StringBuilder();
		locStr.append(country).append(CharPool.POUND );
		locStr.append(city).append(CharPool.POUND);
		locStr.append(ipResult.getLatitude()).append(CharPool.POUND);
		locStr.append(ipResult.getLongitude()).append(CharPool.POUND);
		locStr.append(region);		
		if(locationFromFtel != null){			
			locStr.append(CharPool.POUND).append(locationFromFtel);	
		} else {
			locStr.append(CharPool.POUND);
		}
		String loc = locStr.toString();
		
		System.out.println(loc);
		final long locId = HashUtil.hashUrl128Bit(loc);		
		new RedisCommand<Boolean>(jedisLocationDataStats) {
			@Override
			protected Boolean build() throws JedisException {								
				String keyLoc = "lc:"+locId;
//				String rs = jedis.get(keyLoc);							
//				if(StringUtil.isEmpty(rs)){
//					jedis.set(keyLoc, loc);
//				}
				
				String keyD = "l:"+dateStr;
				String keyH = "l:"+dateHourStr;	
				Pipeline p = jedis.pipelined();
				p.set(keyLoc, loc);
				if(LOCATION_METRIC_PLAYVIEW.equals(metric)){
					//key total
					String keyLocStats = "lcs:"+locId;
					p.incr(keyLocStats);	
				}				
				//time-series keys
				p.hincrBy(keyD, locId +"-"+metric, 1L);
				p.expire(keyD, AFTER_30_DAYS);	
				p.hincrBy(keyH, locId +"-"+metric, 1L);
				p.expire(keyH, AFTER_7_DAYS);	
				p.sync();
				
				return true;
			}
		}.execute();
		
		return locId;
	}
	
	public static final String LOCATION_METRIC_PLAYVIEW = "lplv";
	public static final String LOCATION_METRIC_IMPRESSION = "limp";
	public static final String LOCATION_METRIC_CLICK= "lclk";
	
}
