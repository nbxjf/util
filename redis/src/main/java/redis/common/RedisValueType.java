package redis.common;


public interface RedisValueType<V> {
    /**
     * 将redis值转换为字节数组进行存储
     *
     * @param value 要保存到redis中的值
     * @return 返回转换后得到的字节数组
     */
    byte[] toBytes(V value);

    /**
     * 从字节数组中解析除redis值
     *
     * @param bytes 要解析的字节数组
     * @return 解析得到的值
     */
    V fromBytes(byte[] bytes);
}
