package core;

import java.io.Serializable;

/**
 * 聚合根，DDD核心，提供领域职责、生成领域事件。Aggregate中生成DomainEvent，在DomainService中通过 {@link IDomainEventPublisher}发布事件。
 * <p>
 * 原则：通过唯一标识引用其他聚合 解释：在当前聚合中直接引用其他聚合或者在当前域中调用某些资源类，都会产生耦合，这种耦合会破坏域的完整性。
 * <p>
 * 比如多个订单实体关联同一个用户积分账户，当订单确认收货后需要更改用户积分余额，如果在订单聚合中直接操作用户积分账户，会引入并发更新的问题。
 * <p>
 * 而只保留用户积分账户的标识，对于账户余额的变更通过 领域事件来通知，解除了订单与积分域的耦合，订单域不再关心积分域对于账户余额管理的细节，可以灵活的选择一致性策略
 * <p>
 * (基于分布式事务框架的强一致性、基于消息的最终一致性等){@link IDomainEventPublisher}。
 *
 * <pre>{@code
 * public class Order implements IAggregate<String> {
 *     //领域事件存放容器
 *     private List<IDomainEvent> domainEvents = Lists.newArrayList();
 *
 *
 *     public Order(...) {
 *         ....
 *     }
 *
 *     @Override
 *     public List<IDomainEvent> getDomainEvents() {
 *         return this.domainEvents;
 *     }
 *
 *     //订单号作为订单聚合根的唯一标识
 *     @Override
 *     public String identity() {
 *         return this.orderDTO.getIncrementId();
 *     }
 *
 *     //职责：确认收货
 *     public void confirmSubOrderItem(SubOrderConfirmCommand command) {
 *         //修改订单状态(当前域数据修改)
 *         ...
 *         //生成领域事件
 *         addDomainEvent(new OrderConfirmedEvent(command));
 *     }
 * }
 * }</pre>
 *
 * @param <ID> 聚合根唯一标识 {@link Identifiable}
 * @author Jeff_xu
 * @date 2021/1/8
 * @see IDomainEventPublisher
 */
public interface IAggregate<ID extends Serializable> extends IDomainEventProducer, Identifiable<ID> {
}
