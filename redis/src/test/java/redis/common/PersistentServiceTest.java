package redis.common;

import com.alibaba.fastjson.JSON;

import org.junit.Before;
import org.junit.Test;
import redis.persistent.RedisPersistentService;

/**
 * Created by Jeff_xu on 2021/8/31.
 *
 * @author Jeff_xu
 */
public class PersistentServiceTest extends BaseTest {

    private RedisPersistentService persistentService;

    @Before
    public void setUp() {
        super.setUp();
        persistentService = new RedisPersistentService(redisPool);
    }

    @Test
    public void stringTest() {
        final RedisMap<String, Student> map = persistentService.string("student", Student.class);
        final Student xujifa = map.get("xujifa");
        System.out.println(JSON.toJSONString(xujifa));
    }
}
