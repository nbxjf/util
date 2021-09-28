package redis.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量版本的值加载器，用于加载一组给定的key对应的值，并以key-value形式返回
 *
 * @param <K> key类型
 * @param <V> value类型
 */
@FunctionalInterface
public interface RedisCacheLoader<K, V> {
    /**
     * 加载给定的key对应的value
     *
     * @param keys 要计算的key列表
     * @return 返回传入的每个key以及其对应的value
     */
    Map<K, V> load(List<K> keys);

    /**
     * 单key版本的值加载器，用于加载一个给定的key对应的值
     * 在无批量加载值无性能优势时，可以使用此简化版的值加载器
     *
     * @param <K> key类型
     * @param <V> value类型
     */
    @FunctionalInterface
    interface Simple<K, V> extends RedisCacheLoader<K, V> {
        V loadOne(K key);

        /**
         * 加载给定的key对应的value
         *
         * @param keys 要计算的key列表
         * @return 返回传入的每个key以及其对应的value
         */
        @Override
        default Map<K, V> load(List<K> keys) {
            Map<K, V> map = new HashMap<>(keys.size());
            for (K key : keys) {
                map.put(key, loadOne(key));
            }
            return map;
        }
    }
}
