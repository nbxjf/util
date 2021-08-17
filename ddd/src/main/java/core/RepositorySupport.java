package core;

import java.io.Serializable;

import lombok.NonNull;

/**
 * 领域实体仓库扩展实现，搭配{@link IChangeTraceable}使用，聚合履行职责后，通过IChangeTraceable#getChangeInfo获得变更数据集进行精准更新
 * <p>
 * 1.建立snapshot
 * <pre>{@code
 *     //聚合实现IChangeTraceableAggregate对应的attach/detach方法
 *     @DomainEntity
 *     @Slf4j
 *     @ToString
 *     public abstract class AbstractPayingOrder implements IChangeTraceableAggregate<String, PayingOrderChangeInfo> {
 *         //订单信息
 *         @Getter
 *         protected NewOrderDTOWithBLOBs orderDTO;
 *
 *         // 通过mybatis-generator-plugin (https://gitlab.yit.com/backend/mybatis-generator-plugin/blob/master/src/main/java/com/yit/mybatisplugin/TracerPlugin.java)，生成数据层diff工具；
 *         protected transient NewOrderDTOWithBLOBs.Tracer orderDTOTracer;
 *
 *         public AbstractPayingOrder(@NonNull NewOrderDTOWithBLOBs orderDTO) {
 *             this.orderDTO = orderDTO;
 *             // ...
 *         }
 *
 *         @Override
 *         public void attach() {
 *             //追踪orderDTO变更
 *             this.orderDTOTracer = this.orderDTO.new Tracer();
 *             this.orderDTOTracer.attach();
 *         }
 *
 *         @Override
 *         public void detach() {
 *             if (this.orderDTOTracer != null) {
 *                 this.orderDTOTracer.detach();
 *             }
 *         }
 *     }
 *     //DomainRepository的查询聚合的函数执行后回调IChangeTraceableAggregate#attach建立snapshot
 *     public abstract class RepositorySupport<A extends IChangeTraceableAggregate<ID, D>, ID extends Serializable, D> implements IRepository<A, ID> {
 *         // find 回调
 *         protected abstract A onFind(ID id);
 *
 *         @Override
 *         public A find(@NonNull ID id) {
 *           A aggregate = this.onFind(id);
 *           if (aggregate != null) {
 *               // 这里的就是让查询出来的对象能够被追踪。
 *               // 如果自己实现了一个定制查询接口，要记得单独调用attach。
 *               aggregate.attach();
 *           }
 *           return aggregate;
 *         }
 *         //...
 *     }
 * }</pre>
 * 2.获取增量数据变化
 * <pre>{@code
 * // 聚合实现IChangeTraceableAggregate的获取增量变化的方法
 *     @DomainEntity
 *     @Slf4j
 *     @ToString
 *     public abstract class AbstractPayingOrder implements IChangeTraceableAggregate<String, PayingOrderChangeInfo> {
 *         @Getter
 *         protected NewOrderDTOWithBLOBs orderDTO;
 *
 *         protected transient NewOrderDTOWithBLOBs.Tracer orderDTOTracer;
 *
 *         public AbstractPayingOrder(@NonNull NewOrderDTOWithBLOBs orderDTO) {
 *             this.orderDTO = orderDTO;
 *             // ...
 *         }
 *         //...
 *         @Override
 *         public PayingOrderChangeInfo getChangeInfo() {
 *             //调用diff工具返回增量变化，参看：https://gitlab.yit.com/backend/mybatis-generator-plugin/blob/master/src/main/java/com/yit/mybatisplugin/TracerPlugin.java
 *             NewOrderDTOWithBLOBs orderDTOWithBLOBs = this.orderDTOTracer.getChangeInfo();
 *             if (orderDTOWithBLOBs != null) {
 *                 orderDTOWithBLOBs.setUpdatedAt(new Date());
 *             }
 *             return new PayingOrderChangeInfo(orderDTOWithBLOBs);
 *         }
 *     }
 *     //DomainRepository的update函数回调IChangeTraceableAggregate#getChangeInfo获取增量变化
 *     public abstract class RepositorySupport<A extends IChangeTraceableAggregate<ID, D>, ID extends Serializable, D> implements IRepository<A, ID> {
 *         @Override
 *         public void update(@NonNull A aggregate) {
 *             // 调用UPDATE
 *             this.onUpdate(aggregate.getChangeInfo());
 *             // 重新追踪（清理旧快照，新建快照）
 *             aggregate.detach();
 *             aggregate.attach();
 *         }
 *     }
 *
 *     @Component
 *     public class PayingOrderRepository extends RepositorySupport<AbstractPayingOrder, String, PayingOrderChangeInfo> {
 *         @Override
 *         protected AbstractPayingOrder onFind(String orderNumber) {
 *           //...
 *         }
 *         @Override
 *         protected void onUpdate(PayingOrderChangeInfo payingOrderChangeInfo) {
 *             //持久化层不再关心不同业务场景内变化的具体数据字段
 *             if (payingOrderChangeInfo.getUpdateOrderDTO() != null) {
 *                 orderRepository.updateByPrimaryKeySelective(payingOrderChangeInfo.getUpdateOrderDTO());
 *             }
 *         }
 *     }
 * }</pre>
 *
 * @param <A>  聚合
 * @param <ID> 聚合唯一标识
 * @param <D>  差异数据集
 */
public abstract class RepositorySupport<A extends IChangeTraceableAggregate<ID, D>, ID extends Serializable, D> implements IRepository<A, ID> {
    /**
     * find 回调
     *
     * @param id 聚合唯一标识
     * @return 聚合
     */
    protected abstract A onFind(ID id);

    /**
     * add 回调
     *
     * @param aggregate 新增的领域实体
     */
    protected abstract void onAdd(@NonNull A aggregate);

    /**
     * update 回调
     *
     * @param data 需要更新的数据集，D中的差异数据级包含新增、修改、删除的数据
     */
    protected abstract void onUpdate(@NonNull D data);

    /**
     * remove 回调，清理领域实体相关的数据
     *
     * @param aggregate 删除的领域实体
     */
    protected abstract void onRemove(@NonNull A aggregate);

    @Override
    public A find(@NonNull ID id) {
        A aggregate = this.onFind(id);
        if (aggregate != null) {
            // 这里的就是让查询出来的对象能够被追踪。
            // 如果自己实现了一个定制查询接口，要记得单独调用attach。
            aggregate.attach();
        }
        return aggregate;
    }

    @Override
    public void add(@NonNull A aggregate) {
        // onInsert回调中会回写数据
        this.onAdd(aggregate);
        // 重新追踪（清理旧快照，新建快照）
        aggregate.detach();
        aggregate.attach();
    }

    @Override
    public void remove(@NonNull A aggregate) {
        this.onRemove(aggregate);
        // 删除停止追踪
        aggregate.detach();
    }

    @Override
    public void update(@NonNull A aggregate) {
        // 调用UPDATE
        this.onUpdate(aggregate.getChangeInfo());
        // 重新追踪（清理旧快照，新建快照）
        aggregate.detach();
        aggregate.attach();
    }
}