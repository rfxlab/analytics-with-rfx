package pageview.util;

import common.utils.RealtimeTrackingUtil;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import rfx.core.configs.RedisConfigs;
import rfx.core.nosql.jedis.RedisCommand;
import rfx.core.util.DateTimeUtil;

import java.util.Date;

/**
 * Created by duhc on 28/08/2015.
 */
public class UserRedisUtil {

    final public static ShardedJedisPool redisAdDataStats = RedisConfigs.load().get("adDataStats").getShardedJedisPool();
    public static final int AFTER_3_DAYS = 60 * 60 * 24 * 3;
    public static final int AFTER_7_DAYS = 60 * 60 * 24 * 7;

    public static boolean addPlayViewUser(int unixTime, int placementId, final String uuid) {
        return addUser("up:", unixTime, placementId, uuid);
    }

    public static boolean addImpresisonUser(int unixTime, int placementId, final String uuid) {
        return addUser("ui:", unixTime, placementId, uuid);
    }

    public static boolean addClickUser(int unixTime, int placementId, final String uuid) {
        return addUser("uc:", unixTime, placementId, uuid);
    }

    public static boolean addTrackingUser(int unixTime, int placementId, final String uuid) {
        return addUser("ut:", unixTime, placementId, uuid);
    }

    public static boolean addTrueViewUser(int unixTime, int placementId, final String uuid) {
        return addUser("uv:", unixTime, placementId, uuid);
    }

    public static boolean addUser(String keyPrefix, int unixTime, int placementId, final String uuid) {
        Date date = new Date(unixTime * 1000L);
        final String dateStr = DateTimeUtil.formatDate(date, RealtimeTrackingUtil.DATE_FORMAT_PATTERN);

        return new RedisCommand<Boolean>(redisAdDataStats) {
            @Override
            protected Boolean build() throws JedisException {
                Pipeline p = jedis.pipelined();
                String keyTotal = keyPrefix + "t";
                String keyDaily = keyPrefix + dateStr + ":t";
                String keyHourly = keyPrefix + dateStr + ":" + placementId;

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

    public static boolean addReachUser(int unixTime, int crtId, final String uuid) {
        Date date = new Date(unixTime * 1000L);
        final String dateStr = DateTimeUtil.formatDate(date, RealtimeTrackingUtil.DATE_FORMAT_PATTERN);

        return new RedisCommand<Boolean>(redisAdDataStats) {
            @Override
            protected Boolean build() throws JedisException {
                Pipeline p = jedis.pipelined();
                String keyTotal = "cru:t:" + crtId;
                String key = "cru:" + dateStr + ":" + crtId;

                p.pfadd(keyTotal, uuid);
                p.pfadd(key, uuid);
                p.expire(key, AFTER_3_DAYS);

                p.sync();
                return true;
            }
        }.execute();
    }

}
