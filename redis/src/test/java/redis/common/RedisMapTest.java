package redis.common;

import java.util.Arrays;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import org.junit.Before;
import org.junit.Test;
import utils.serializer.FastJsonSerializer;

/**
 * Created by Jeff_xu on 2021/8/27.
 *
 * @author Jeff_xu
 */
public class RedisMapTest extends BaseTest {

    private static final String KEY_SPACE = "test/String";

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testSet() {
        RedisMap<Student, Student> map = new RedisMap<>(redisPool, KEY_SPACE, RedisKeyType.normal(), new DefaultRedisValueType<>(Student.class, new FastJsonSerializer()));
        final Student xujifa = new Student("xujifa", 5);
        map.set(xujifa, xujifa);
    }

    @Test
    public void testSet_2() {
        RedisMap<Men, Men> map = new RedisMap<>(redisPool, KEY_SPACE, RedisKeyType.normal(), new DefaultRedisValueType<>(Men.class, new FastJsonSerializer()));
        final Men xujifa = new Men("xujifa", 5);
        map.set(xujifa, xujifa);
    }

    @Test
    public void testSet_3() {
        RedisMap<Men, Men> map = new RedisMap<>(redisPool, KEY_SPACE, RedisKeyType.normal(), new DefaultRedisValueType<>(Men.class, new FastJsonSerializer()));
        final Men xujifa2 = new Men("xujifa2", 10);
        map.set(xujifa2, xujifa2, 10000);
    }

    @Test
    public void testGet() {
        RedisMap<Student, Student> map = new RedisMap<>(redisPool, KEY_SPACE, RedisKeyType.normal(), new DefaultRedisValueType<>(Student.class, new FastJsonSerializer()));
        final Student xujifa = new Student("xujifa", 5);
        final Student student = map.get(xujifa);
        System.out.println(JSON.toJSONString(student));
    }

    @Test
    public void testGet_2() {
        RedisMap<Men, Men> map = new RedisMap<>(redisPool, KEY_SPACE, RedisKeyType.normal(), new DefaultRedisValueType<>(Men.class, new FastJsonSerializer()));
        final Men xujifa = new Men("xujifa", 5);
        final Men student = map.get(xujifa);
        System.out.println(JSON.toJSONString(student));
    }

    @Test
    public void testGet_3() {
        RedisMap<Student, Student> map = new RedisMap<>(redisPool, KEY_SPACE, RedisKeyType.normal(), new DefaultRedisValueType<>(Student.class, new FastJsonSerializer()));
        final Student xujifa = new Student("xujifa", 5);
        final Student xujifa1 = new Student("xujifa1", 6);
        Map<Student, Student> studentStudentMap = map.get(Arrays.asList(xujifa, xujifa1));

        System.out.println(JSON.toJSONString(studentStudentMap));
        map.set(xujifa1, xujifa1);
        studentStudentMap = map.get(Arrays.asList(xujifa, xujifa1));
        System.out.println(JSON.toJSONString(studentStudentMap));
    }

}
