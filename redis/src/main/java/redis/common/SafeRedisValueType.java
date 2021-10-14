package redis.common;

import lombok.extern.slf4j.Slf4j;
import redis.common.SafeRedisValueType.ExceptionalValue;
import utils.serializer.Serializer;

/**
 * Created by Jeff_xu on 2021/9/30.
 *
 * @author Jeff_xu
 */
@Slf4j
public class SafeRedisValueType<V> implements RedisValueType<ExceptionalValue<V>> {

    private final RedisValueType<V> valueType;

    public SafeRedisValueType(Class<V> clazz, Serializer serializer) {
        this.valueType = new RedisValueInstance<>(clazz, serializer);
    }

    public SafeRedisValueType(RedisValueType<V> valueType) {
        this.valueType = valueType;
    }

    @Override
    public byte[] toBytes(ExceptionalValue<V> value) {
        return valueType.toBytes(value.getValue());
    }

    @Override
    public ExceptionalValue<V> fromBytes(byte[] bytes) {
        try {
            return new ExceptionalValue<>(valueType.fromBytes(bytes), null);
        } catch (Exception e) {
            log.error("deserialize from bytes failed.valueType:{}", valueType, e);
            return new ExceptionalValue<>(null, e);
        }
    }

    /**
     * 记录redis的返回结果值，包含具体的值以及exception
     *
     * @param <T>
     */
    public static class ExceptionalValue<T> {
        private final T value;
        private final Exception exception;

        public ExceptionalValue(T value, Exception exception) {
            this.value = value;
            this.exception = exception;
        }

        public T getValue() {
            if (exception != null) {
                throw new IllegalStateException(exception);
            }
            return value;
        }

        public Exception getException() {
            return exception;
        }

        public boolean isFailed() {
            return exception != null;
        }
    }
}
