package core;

import java.io.Serializable;

/**
 * 聚合根履行职责的同时追踪数据变化 {@link IChangeTraceable} {@link IAggregate}
 *
 * @param <ID> 聚合根唯一标识 {@link IAggregate}
 * @param <D>  变更的数据集 {@link IChangeTraceable}
 * @author Jeff_xu
 * @date 2021/1/19
 */
public interface IChangeTraceableAggregate<ID extends Serializable, D> extends IChangeTraceable<D>, IAggregate<ID> {
}
