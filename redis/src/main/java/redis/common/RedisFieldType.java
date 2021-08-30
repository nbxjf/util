package redis.common;

import java.math.BigDecimal;

public interface RedisFieldType<F> {

    String toString(F field);

    F fromString(String field);

    RedisFieldType<String> STRING = new RedisFieldType<String>() {
        @Override
        public String toString(String field) {
            return field;
        }

        @Override
        public String fromString(String field) {
            return field;
        }
    };

    RedisFieldType<Byte> BYTE = new RedisFieldType<Byte>() {
        @Override
        public String toString(Byte field) {
            return field != null ? field.toString() : null;
        }

        @Override
        public Byte fromString(String field) {
            return field != null ? Byte.parseByte(field) : null;
        }
    };

    RedisFieldType<Short> SHORT = new RedisFieldType<Short>() {
        @Override
        public String toString(Short field) {
            return field != null ? field.toString() : null;
        }

        @Override
        public Short fromString(String field) {
            return field != null ? Short.parseShort(field) : null;
        }
    };

    RedisFieldType<Integer> INTEGER = new RedisFieldType<Integer>() {
        @Override
        public String toString(Integer field) {
            return field != null ? field.toString() : null;
        }

        @Override
        public Integer fromString(String field) {
            return field != null ? Integer.parseInt(field) : null;
        }
    };

    RedisFieldType<Long> LONG = new RedisFieldType<Long>() {
        @Override
        public String toString(Long field) {
            return field != null ? field.toString() : null;
        }

        @Override
        public Long fromString(String field) {
            return field != null ? Long.parseLong(field) : null;
        }
    };

    RedisFieldType<BigDecimal> BIG_DECIMAL = new RedisFieldType<BigDecimal>() {
        @Override
        public String toString(BigDecimal field) {
            return field != null ? field.toString() : null;
        }

        @Override
        public BigDecimal fromString(String field) {
            return field != null ? new BigDecimal(field) : null;
        }
    };
}
