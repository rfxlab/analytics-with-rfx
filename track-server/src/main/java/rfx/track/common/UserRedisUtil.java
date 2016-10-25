package rfx.track.common;

import java.util.Date;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import rfx.core.configs.RedisConfigs;
import rfx.core.nosql.jedis.RedisCommand;
import rfx.core.util.DateTimeUtil;

/**
 * Created by trieu on 28/08/2015.
 */
public class UserRedisUtil {

    final public static ShardedJedisPool redisAdDataStats = RedisConfigs.load().get("adDataStats").getShardedJedisPool();
    public static final int AFTER_3_DAYS = 60 * 60 * 24 * 3;
    public static final int AFTER_7_DAYS = 60 * 60 * 24 * 7;

    public static boolean addPlayViewUser(int unixTime, String metric, final String uuid) {
        return addUser("up:", unixTime, metric, uuid);
    } 

    public static boolean addUser(String keyPrefix, int unixTime, String metric, final String uuid) {
        Date date = new Date(unixTime*1000L);
        final String dateStr = DateTimeUtil.formatDate(date, DateTimeUtil.DATE_FORMAT_PATTERN);

        return new RedisCommand<Boolean>(redisAdDataStats) {
            @Override
            protected Boolean build() throws JedisException {
                Pipeline p = jedis.pipelined();
                String keyTotal = keyPrefix+"t";
                String keyDaily = keyPrefix+dateStr+":t";
                String keyHourly = keyPrefix+dateStr+":"+metric;

                p.pfadd(keyTotal, uuid);
                p.pfadd(keyDaily, uuid);
                p.pfadd(keyHourly, uuid);
                p.expire(keyDaily, AFTER_7_DAYS);
                p.expire(keyHourly, AFTER_3_DAYS);

                p.sync();
                return true;
            }
        }.execute();
    }

   

}

