package redis.common;

import utils.serializer.Serializer;

public class RedisValueInstance<V> implements RedisValueType<V> {

    private final Class<V> clazz;
    private final Serializer serializer;

    public RedisValueInstance(Class<V> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    public byte[] toBytes(V value) {
        return serializer.toBytes(value);
    }

    @Override
    public V fromBytes(byte[] bytes) {
        return serializer.toObject(bytes, clazz);
    }
}
