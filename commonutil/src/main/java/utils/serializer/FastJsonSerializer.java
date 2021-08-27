package utils.serializer;

import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

public class FastJsonSerializer implements Serializer {

    /**
     * 对象序列化成字符串
     *
     * @param value 支持序列化的对象
     * @return 序列化字符串
     */
    @Override
    public String toString(Object value) {
        if (value == null) {
            return null;
        }
        return new String(toBytes(value), StandardCharsets.UTF_8);
    }

    /**
     * 对象序列化成byte数组
     *
     * @param value 支持序列化的对象
     * @return 序列化byte数组
     */
    @Override
    public byte[] toBytes(Object value) {
        if (value == null) {
            return new byte[0];
        }
        return JSON.toJSONBytes(value);
    }

    /**
     * 序列化字符串转换成对象
     *
     * @param value 序列化字符串
     * @param clazz 类型
     * @return 对象
     */
    @Override
    public <T> T toObject(String value, Class<T> clazz) {
        if (value == null) {
            return null;
        }
        return toObject(value.getBytes(StandardCharsets.UTF_8), clazz);
    }

    /**
     * 序列化byte数组转换成对象
     *
     * @param bytes 序列化byte数组
     * @param clazz 类型
     * @return 对象
     */
    @Override
    public <T> T toObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return JSON.parseObject(bytes, clazz, Feature.DisableFieldSmartMatch);
    }
}
