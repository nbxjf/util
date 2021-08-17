package core;

/**
 * 领域事件发布器，发布并处理聚合根履行职责时生成的领域事件，用来做跨域通信，将变化通过事件模型传递给其他域。
 * <p>
 * 原则：尽可能在领域边界之外使用最终一致性
 * <p>
 * 解释：跨域的通信保持数据的实时一致性的成本是巨大的，在允许一定程度的跨域数据不一致的场景使用最终一致性减少响应时长。
 * <p>
 * 实现策略： {@link DomainEventBus, DomainEventTransactionalPublisher}
 *
 * @author Jeff_xu
 * @date 2021/2/23
 */
public interface IDomainEventPublisher {
    /**
     * 立即发送领域事件
     */
    void publish(IDomainEventProducer producer);
}
