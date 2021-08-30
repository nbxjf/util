package redis.common;

/**
 * Created by Jeff_xu on 2021/8/27.
 *
 * @author Jeff_xu
 */
public class DefaultRedisKeyType<K> implements RedisKeyType<K> {

    private final Class<K> kClass;

    public DefaultRedisKeyType(Class<K> kClass) {
        this.kClass = kClass;
    }

    @Override
    public String toString(K key) {
        return kClass.toString();
    }
}
