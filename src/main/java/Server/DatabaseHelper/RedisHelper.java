package Server.DatabaseHelper;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;

public class RedisHelper {

    public static void logout(String token) {
        Jedis jedis = null;
        try {
            jedis = RedisDBPool.getJedis();
            jedis.del(token);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisDBPool.close(jedis);
        }
    }

    public static void login(String token, List<HashMap<String, List<String>>> authList, List<String> authServer) {
        Jedis jedis = null;
        try {
            jedis = RedisDBPool.getJedis();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisDBPool.close(jedis);
        }
    }
}
