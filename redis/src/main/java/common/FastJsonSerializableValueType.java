package common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

public class FastJsonSerializableValueType<V> implements RedisValueType<V> {

    private final Class<V> clazz;

    public FastJsonSerializableValueType(Class<V> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] toBytes(V value) {
        if (value == null) {
            return new byte[0];
        }
        return JSON.toJSONBytes(value);
    }

    @Override
    public V fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return JSON.parseObject(bytes, clazz, Feature.DisableFieldSmartMatch);
    }
}
