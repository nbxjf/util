package deepclone;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;

/**
 * Created by Jeff_xu on 2019/10/11.
 *
 * @author Jeff_xu
 */
@Slf4j
public class DeepCloneUtil {

    private DeepCloneUtil() {}

    private static ConcurrentMap<Class<?>, BeanCopier> beanCopiesMap = new ConcurrentHashMap<>();

    /**
     * Deep clone
     * 使用限制：使用实体BeanCopier进行赋值，要求实体必须有getter(),setter()方法
     *
     * @param source 需要克隆的实体
     * @param <T>    type
     * @return clone entity
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T source) {
        try {
            Object result;
            if (Objects.isNull(source)) {
                return null;
            } else if (isBasicElementType(source)) {
                // 基本数据类型直接返回
                result = source;
            } else if (source.getClass().isArray()) {
                // 数组
                int length = Array.getLength(source);
                Object cloneInstance = Array.newInstance(source.getClass().getComponentType(), length);
                for (int i = 0; i < length; i++) {
                    Array.set(cloneInstance, i, deepClone(Array.get(source, i)));
                }
                result = cloneInstance;
            } else if (source instanceof Collection) {
                // 集合类型
                Object cloneInstance = source.getClass().newInstance();
                ((Collection)source).forEach(o -> ((Collection)cloneInstance).add(deepClone(o)));
                result = cloneInstance;
            } else if (source instanceof Map) {
                // Map类型
                Object cloneInstance = source.getClass().newInstance();
                Map<Object, Object> values = (Map)source;
                values.forEach((key, value) -> ((Map)cloneInstance).put(key, deepClone(value)));
                result = cloneInstance;
            } else {
                // 其他对象
                Object cloneInstance = source.getClass().newInstance();
                BeanCopier copier = createCopier(source.getClass());
                copier.copy(source, cloneInstance, (pojo, fieldType, fieldName) -> deepClone(pojo));
                result = cloneInstance;
            }
            return (T)result;
        } catch (Exception e) {
            throw new RuntimeException("Deep clone failed,source");
        }
    }

    /**
     * 判断是否是基础数据类型
     *
     * @param bean 对象
     * @param <T>  type
     * @return 是否是基础数据类型
     */
    private static <T> boolean isBasicElementType(T bean) {
        Class<?> clazz = bean.getClass();
        return Boolean.class.equals(clazz) || boolean.class.equals(clazz) ||
            Byte.class.equals(clazz) || byte.class.equals(clazz) ||
            Character.class.equals(clazz) || char.class.equals(clazz) ||
            Double.class.equals(clazz) || double.class.equals(clazz) ||
            Float.class.equals(clazz) || float.class.equals(clazz) ||
            Integer.class.equals(clazz) || int.class.equals(clazz) ||
            Long.class.equals(clazz) || long.class.equals(clazz) ||
            Short.class.equals(clazz) || short.class.equals(clazz) ||
            String.class.equals(clazz) || Date.class.equals(clazz) ||
            BigDecimal.class.equals(clazz);
    }

    private static BeanCopier createCopier(Class<?> clz) {
        if (beanCopiesMap.containsKey(clz)) {
            return beanCopiesMap.get(clz);
        }
        BeanCopier beanCopier = BeanCopier.create(clz, clz, true);
        beanCopiesMap.putIfAbsent(clz, beanCopier);
        return beanCopier;
    }

    public static void main(String[] args) {
        boolean a = false;
    }
}
