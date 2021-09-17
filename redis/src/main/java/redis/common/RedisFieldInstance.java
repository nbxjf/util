package redis.common;

import java.nio.charset.StandardCharsets;

import utils.serializer.Serializer;

/**
 * Created by Jeff_xu on 2021/9/17.
 *
 * @author Jeff_xu
 */
public class RedisFieldInstance<F> implements RedisFieldType<F> {

    private final Class<F> clazz;
    private final Serializer serializer;

    public RedisFieldInstance(Class<F> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    public String toString(F field) {
        return serializer.toString(field);
    }

    @Override
    public F fromString(String field) {
        return serializer.toObject(field.getBytes(StandardCharsets.UTF_8), clazz);
    }
}
