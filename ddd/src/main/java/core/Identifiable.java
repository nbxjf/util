package core;

import java.io.Serializable;

/**
 * 可被识别的实体，identity为实体的唯一标识
 * <p>
 * tips:ID若为自定义类型，务必实现自定义equals、hash函数
 *
 * @param <ID> 实体标识
 * @author Jeff_xu
 * @date 2021/1/8
 */
public interface Identifiable<ID extends Serializable> {
    ID identity();
}