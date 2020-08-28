package Server.DatabaseHelper;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.InputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RedisDBPool {
    private static JedisPool pool;
    private static JedisPoolConfig config = new JedisPoolConfig();
    private static  int timeOut = 10000;
    private static String host;
    private static int port;
    private static String password;

    public static void init(Element root, ScheduledThreadPoolExecutor executor) throws Exception {
        initConfig(root);
        initPool();
        doCheckInterrupt(executor);
    }

    private static void initPool(){
        try{
            if(pool != null){
                pool.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        pool = new redis.clients.jedis.JedisPool(config,host,port,timeOut,password);
    }

    public static void init(InputStream in, ScheduledThreadPoolExecutor executor) throws Exception {
        init((new SAXBuilder()).build(in).getRootElement(),executor);
    }

    private static void initConfig(Element root) throws Exception{
        Element configElement = root.getChild("config");
        config.setMaxIdle(configElement.getAttribute("MaxIdle").getIntValue());
        config.setMaxTotal(configElement.getAttribute("MaxTotal").getIntValue());
        config.setMinIdle(configElement.getAttribute("MinIdle").getIntValue());
        timeOut = configElement.getAttribute("timeout").getIntValue();

        Element serverElement = root.getChild("server");
        host = serverElement.getAttributeValue("host");
        port = serverElement.getAttribute("port").getIntValue();
        password = serverElement.getAttributeValue("pwd");
    }

    /**
     * 获取默认jedis对象
     * @return jedis对象
     */
    public static Jedis getJedis() {
        return pool == null ? null : pool.getResource();
    }

    /**
     * 根据数据库编号获取jedis对象
     * @param db 数据库分区编号
     * @return jedis对象
     */
    public static Jedis getJedis(int db) {
        Jedis jedis = getJedis();
        if(jedis != null ) {
            jedis.select(db);
        }
        return jedis;
    }

    public static void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    private static void doCheckInterrupt(ScheduledThreadPoolExecutor executor){
        executor.scheduleWithFixedDelay(()->checkInterrupt(),1000,1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 重连Redis
     */
    private static void reConnect(){
        try{
            initPool();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static synchronized void checkInterrupt(){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis == null ||  !"PONG".equals(jedis.ping())) {
                reConnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //获取出错,重连
            reConnect();
        } finally {
            close(jedis);
        }
    }
}
