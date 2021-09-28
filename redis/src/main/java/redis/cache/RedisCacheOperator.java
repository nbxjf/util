package redis.cache;

import java.util.List;
import java.util.Map;

/**
 * Created by Jeff_xu on 2021/9/28.
 * Redis缓存操作器
 * 表示业务上的一类redis缓存，此类缓存拥有共同的缓存描述（包括key前缀，key和value的类型、版本号、未命中时的计算逻辑等）
 *
 * @param <K> 缓存中保存的key的类型
 * @param <V> 缓存中保存的value的类型
 * @author Jeff_xu
 */
public interface RedisCacheOperator<K, V> {

    /**
     * 从redis中获取key的缓存
     * 缓存不存在时，返回null
     *
     * @param key key
     * @return cache obj or null
     */
    V get(K key);

    /**
     * 从缓存中获取一组key的缓存
     *
     * @param keys 一组key
     * @return exists cache obj
     */
    Map<K, V> get(List<K> keys);

    /**
     * 刷新单个缓存
     *
     * @param key   key
     * @param value cache obj
     */
    void refresh(K key, V value);

    /**
     * 刷新一组缓存
     *
     * @param keyValues 缓存数据
     */
    void refresh(Map<K, V> keyValues);

    /**
     * 失效单个缓存
     *
     * @param key key
     */
    void invalid(K key);

    /**
     * 失效一组缓存
     *
     * @param keys keys
     */
    void invalid(List<K> keys);

    /**
     * 判断缓存中是否存在某个key
     *
     * @param key key
     * @return exist or not
     */
    boolean exists(K key);

}
