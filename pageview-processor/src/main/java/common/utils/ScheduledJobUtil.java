package common.utils;

import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import rfx.core.configs.RedisConfigs;
import rfx.core.nosql.jedis.RedisCommand;
import rfx.core.util.StringUtil;

public class ScheduledJobUtil {
	private static final String REDIS_JOB__KEY = "scheduled-jobs";
	static ShardedJedisPool jedisPool = RedisConfigs.load().get("clusterInfoRedis").getShardedJedisPool();	
	
	public static int getHourBackForSynchDataJob(final String jobClassName){		
		return new RedisCommand<Integer>(jedisPool) {
			@Override
			protected Integer build() throws JedisException {
				int hoursback = StringUtil.safeParseInt(jedis.hget(REDIS_JOB__KEY,jobClassName),0);
				if(hoursback == 0){
					hoursback = 2;
					jedis.hset(REDIS_JOB__KEY, jobClassName, String.valueOf(hoursback));
				}
				return hoursback;			
			}
		}.execute();
	}
}
