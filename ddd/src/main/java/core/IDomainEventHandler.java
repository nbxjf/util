package core;

import com.google.common.eventbus.Subscribe;

/**
 * 领域事件处理器，注意在结合DomainEventBus作为事件发布器时，需要显示的在事件处理函数上使用{@link Subscribe}注解才能完成注册
 * <p>
 * 场景一： //将领域事件转换为消息，进行跨域通信，达到最终一致性
 * <pre>{@code
 * @Component
 * public class OrderConfirmedEventHandler implements IDomainEventHandler<OrderConfirmedEvent> {
 *     @Autowired
 *     private TransactionMessagePublisher transactionMessagePublisher;
 *     @Override
 *     @Subscribe
 *     public void handleDomainEvent(OrderConfirmedEvent domainEvent) {
 *         //通知外部系统订单确认完成(领域事件由内存事件转换为消息) 最终一致性
 *         transactionMessagePublisher.enqueue(QueueConfig.ORDER_SERVICE_ORDER_CONFIRM_TOPIC, domainEvent.getSimpleOrderInfo());
 *     }
 * }
 * }</pre>
 * 场景二： //跨域通信，搭配分布式事务TCC，实现强一致性
 * <pre>{@code
 * @Component
 * public class OrderConfirmedEventHandler implements IDomainEventHandler<OrderConfirmedEvent> {
 *     @Autowired
 *     private UserServiceClient userServiceClient;
 *     @Override
 *     @Subscribe
 *     public void handleDomainEvent(OrderConfirmedEvent domainEvent) {
 *         //tcc
 *         userServiceClient.unfrozenPoint(domainEvent.getSimpleOrderInfo());
 *     }
 * }
 * }
 * @param <T> 监听的领域事件类型
 * @author Jeff_xu
 * @date 2021/1/8
 */
public interface IDomainEventHandler<T extends IDomainEvent> {
    void handleDomainEvent(T domainEvent);
}
