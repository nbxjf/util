package redis.common;

import java.util.Objects;

/**
 * 描述redis key的类型
 *
 * @param <K> redis key类型
 */
@FunctionalInterface
public interface RedisKeyType<K> {

    /**
     * 将redis key转换为可解释的字符串类型
     *
     * @param key key
     * @return string
     */
    String toString(K key);

    /**
     * string的默认实现
     */
    RedisKeyType<String> STRING = s -> s;

    /**
     * 默认普遍实现方式，直接toString
     *
     * @param <K> key type
     * @return string
     */
    static <K> RedisKeyType<K> normal() {
        return Objects::toString;
    }
}
