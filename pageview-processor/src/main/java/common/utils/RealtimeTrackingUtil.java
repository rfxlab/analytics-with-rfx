package common.utils;

import java.util.Date;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import rfx.core.configs.RedisConfigs;
import rfx.core.nosql.jedis.RedisCommand;
import rfx.core.util.DateTimeUtil;


public class RealtimeTrackingUtil {

	private static final String MONITOR_PREFIX = "m:";
	private static final String YYYY_MM_DD_HH = "yyyy-MM-dd-HH";
			
	public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
	public static final String DATE_HOUR_FORMAT_PATTERN = YYYY_MM_DD_HH;
	public static final String DATE_HOUR_MINUTE_FORMAT_PATTERN = "yyyy-MM-dd-HH-mm";
	public static final String DATE_HOUR_MINUTE_SECOND_FORMAT_PATTERN = "yyyy-MM-dd-HH-mm-ss";
	final static int SIX_MONTHS = 262974*60;

	private static final int AFTER_1_DAY = 86400;
	private static final int AFTER_2_DAYS = AFTER_1_DAY * 2;
	private static final int AFTER_4_DAYS = AFTER_1_DAY * 4;
	
		
	static ShardedJedisPool jedisPool = RedisConfigs.load().get("realtimeDataStats").getShardedJedisPool();
		
	public static boolean updateKafkaLogEvent(int unixtime, final String event){
		Date date = new Date(unixtime*1000L);
		final String dateStr = DateTimeUtil.formatDate(date ,DATE_FORMAT_PATTERN);
		final String dateHourStr = DateTimeUtil.formatDate(date ,YYYY_MM_DD_HH);
		
		boolean commited = new RedisCommand<Boolean>(jedisPool) {
			@Override
			protected Boolean build() throws JedisException {
				String keyD = MONITOR_PREFIX+dateStr;
				String keyH = MONITOR_PREFIX+dateHourStr;
				
				Pipeline p = jedis.pipelined();				
				p.hincrBy(keyD, "e:"+event , 1L);
				p.expire(keyD, AFTER_4_DAYS);
				p.hincrBy(keyH, "e:"+event , 1L);
				p.expire(keyH, AFTER_2_DAYS);
				p.sync();
				return true;
			}
		}.execute();
		
		return commited;
	}
	
	public static boolean updateMonitorEvent(int unixtime, final String event){
		Date date = new Date(unixtime*1000L);
		final String dateStr = DateTimeUtil.formatDate(date ,DATE_FORMAT_PATTERN);
		final String dateHourStr = DateTimeUtil.formatDate(date ,YYYY_MM_DD_HH);
		
		boolean commited = new RedisCommand<Boolean>(jedisPool) {
			@Override
			protected Boolean build() throws JedisException {
				String keyD = MONITOR_PREFIX+dateStr;
				String keyH = MONITOR_PREFIX+dateHourStr;				
				Pipeline p = jedis.pipelined();				
				p.hincrBy(keyD, event , 1L);
				p.expire(keyD, AFTER_2_DAYS);
				p.hincrBy(keyH, event , 1L);
				p.expire(keyH, AFTER_1_DAY);
				p.sync();
				return true;
			}
		}.execute();		
		return commited;
	}

}

