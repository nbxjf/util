package redis.common;

/**
 * Created by Jeff_xu on 2021/8/27.
 *
 * @author Jeff_xu
 */
public class RedisKeyInstance<K> implements RedisKeyType<K> {

    private final Class<K> clazz;

    public RedisKeyInstance(Class<K> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString(K key) {
        return clazz.toString();
    }
}
