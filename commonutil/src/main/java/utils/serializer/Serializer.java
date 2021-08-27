package utils.serializer;

/**
 * 对象序列封装
 */
public interface Serializer {

    /**
     * 对象序列化成字符串
     *
     * @param value 支持序列化的对象
     * @return 序列化字符串
     */
    String toString(Object value);

    /**
     * 对象序列化成byte数组
     *
     * @param value 支持序列化的对象
     * @return 序列化byte数组
     */
    byte[] toBytes(Object value);

    /**
     * 序列化字符串转换成对象
     *
     * @param value 序列化字符串
     * @param clazz 类型
     * @param <T>   类型
     * @return 对象
     */
    <T> T toObject(String value, Class<T> clazz);

    /**
     * 序列化byte数组转换成对象
     *
     * @param bytes 序列化byte数组
     * @param clazz 类型
     * @param <T>   类型
     * @return 对象
     */
    <T> T toObject(byte[] bytes, Class<T> clazz);
}
