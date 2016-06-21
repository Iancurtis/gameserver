import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Administrator on 2016/1/21.
 */
public class TestRedis {
    @Test
    public void testRedis(){


        Jedis jedis = new Jedis("192.168.198.133", 15600);
        jedis.auth("123456");

        Long time = System.currentTimeMillis();
        jedis.set("hello", "world");
        System.out.println(jedis.get("hello"));
        System.out.println(System.currentTimeMillis() - time);

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(200);
        config.setMaxWaitMillis(10000);
        config.setTestOnBorrow(true);
//192.168.198.133:15600
        JedisPool jedisPool = new JedisPool(config, "192.168.198.133", 15600, 10000, "123456");
        jedis = jedisPool.getResource();

        int index = 2000000;
        while (index < 3000000){
            try {
                jedis.set("hello" + index, "world" + index);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                System.out.println("--------JedisPool--------------");
                jedisPool = new JedisPool(config, "192.168.198.132", 15600, 10000, "123456");  //切换节点
                jedis = jedisPool.getResource();
            }

            index = index + 1;
        }
        time = System.currentTimeMillis();

        jedis.hset("hell233", "x", "2");
        System.out.println(jedis.get("hello1"));
        System.out.println(System.currentTimeMillis() - time);

        System.out.println(System.currentTimeMillis() - time);
    }
}
