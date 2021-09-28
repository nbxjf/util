package redis.cache;

import java.io.Serializable;

/**
 * Created by Jeff_xu on 2021/9/28.
 * 实际存入redis的缓存实体
 *
 * @author Jeff_xu
 */
public class CacheEntity implements Serializable {

    /**
     * 缓存的实际数据
     * 部分序列化框架不支持顶级对象的动态化，将其封装为字段绕过该限制
     */
    private Object value;

    /**
     * 存入redis的时间，用于逻辑超时判断
     */
    private long timestamp;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
