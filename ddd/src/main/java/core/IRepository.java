package core;

import java.io.Serializable;

import lombok.NonNull;

/**
 * 聚合对应的仓库
 *
 * @param <A>  聚合类型 {@link IAggregate}
 * @param <ID> 实体标识
 */
public interface IRepository<A extends IAggregate<ID>, ID extends Serializable> {
    /**
     * 新增聚合
     *
     * @param aggregate 聚合
     */
    void add(@NonNull A aggregate);

    /**
     * 查询聚合
     *
     * @param id 聚合的唯一标识
     * @return 查询聚合
     */
    A find(@NonNull ID id);

    /**
     * 更新聚合
     *
     * @param aggregate 需要更新的聚合
     */
    void update(@NonNull A aggregate);

    /**
     * 删除聚合
     *
     * @param aggregate 需要删除的聚合
     */
    void remove(@NonNull A aggregate);
}