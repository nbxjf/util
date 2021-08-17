package core;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;
import core.DomainEventTransactionalPublisher.IDomainEventSubscriber.DefaultSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 基于eventBus的领域事件发布器
 * <p>
 * 1.事件发布不在事务体内，使用消息总线模式，事件执行失败不影响事务提交
 * <p>
 * 2.事务内事件派发器，事件发布、派发、执行出异常，回滚事务
 * <p>
 * 注意：eventBus默认的dispatcher实现是{@link com.google.common.eventbus.Dispatcher#perThreadDispatchQueue()}，保证单线程中消息的有序性；
 * <p>
 * 默认的exception处理策略是log异常，不会将异常抛出。可选策略在领域事件处理出现异常时将事件转化为消息，异步重试，实现参看{@link DomainEventTransformToMessageHandler}
 * <p>
 * 默认的event executor为{@link MoreExecutors#directExecutor()}，即在当前线程中进行事件处理，若需要异步的执行处理可使用{@link AsyncEventBus}结合自定义线程池进行使用；
 * <p>
 * 配置说明：spring xml中添加如下配置(采用{@link DomainEventTransformToMessageHandler}作为异常处理方案)
 * <pre>{@code
 * <bean id="exceptionHandler" class="DomainEventTransformToMessageHandler">
 *      <constructor-arg name="kafkaJsonMessageService" ref="kafkaJsonMessageService"/>
 *      <constructor-arg name="retryQueueName" value="Order-domainEventRetryQueue"/>
 *      <constructor-arg name="groupName" value="order"/>
 *      <constructor-arg name="retryThreadCount" value="5"/>
 * </bean>
 * <bean id="eventBus" class="com.google.common.eventbus.EventBus">
 *      <constructor-arg name="exceptionHandler" ref="exceptionHandler"/>
 * </bean>
 * <bean id="domainEventBus" class="DomainEventBus">
 *      <constructor-arg name="eventBus" ref="eventBus"/>
 * </bean>
 * }</pre>
 * <p>
 * 如何发布事件：
 * <pre>{@code
 * public class OrderDomainService {
 *      public void confirmOrder(String orderNumber){
 *          ShippingOrder shippingOrder = shippingOrderRepository.find(orderNumber);
 *          shippingOrder.confirm();
 *          //持久化
 *          shippingOrderRepository.update(shippingOrder);
 *          //发布领域事件
 *          domainEventBus.publish(shippingOrder);
 *      }
 * }}</pre>
 *
 * @author Jeff_xu
 * @date 2021/2/23
 */
@Slf4j
public class DomainEventBus implements ApplicationListener<ContextRefreshedEvent>, IDomainEventPublisher {
    /**
     * 事件发布不在事务体内，使用消息总线模式，事件执行失败不影响事务提交
     */
    private EventBus eventBus;
    /**
     * 事务内事件派发器，事件发布、派发、执行出异常，回滚事务
     */
    private DomainEventTransactionalPublisher transactionalPublisher;

    public DomainEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void publish(IDomainEventProducer producer) {
        if (CollectionUtils.isNotEmpty(producer.getDomainEvents())) {
            try {
                if (TransactionSynchronizationManager.isSynchronizationActive() &&
                    TransactionSynchronizationManager.isActualTransactionActive()) {
                    //在事务块中执行
                    log.debug("publish with transactionalPublisher!");
                    this.transactionalPublisher.publish(producer);
                } else {
                    //非事务块内执行
                    log.debug("publish with eventBus!");
                    for (IDomainEvent domainEvent : producer.getDomainEvents()) {
                        this.eventBus.post(domainEvent);
                    }
                }
            } finally {
                producer.clearDomainEvents();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, IDomainEventHandler> domainEventHandlerMap = event.getApplicationContext().getBeansOfType(IDomainEventHandler.class);
        if (MapUtils.isNotEmpty(domainEventHandlerMap)) {
            //事务内的事件发布器
            DomainEventTransactionalPublisher transactionalPublisher = new DomainEventTransactionalPublisher();
            transactionalPublisher.setSubscriberMap(domainEventHandlerMap.values().stream()
                .map(iDomainEventHandler -> (IDomainEventHandler<IDomainEvent>)iDomainEventHandler)
                .collect(Collectors
                    .groupingBy(iDomainEventHandler -> GenericTypeResolver.resolveTypeArgument(iDomainEventHandler.getClass(), IDomainEventHandler.class),
                        Collectors.mapping(DefaultSubscriber::new, Collectors.toList()))));
            this.transactionalPublisher = transactionalPublisher;
            //无事务的事件派发器
            List<IDomainEventHandler> domainEventHandlers = Lists.newArrayList(domainEventHandlerMap.values());
            for (IDomainEventHandler iDomainEventHandler : domainEventHandlers) {
                Class[] parameterTypes = new Class[] {GenericTypeResolver.resolveTypeArgument(iDomainEventHandler.getClass(), IDomainEventHandler.class)};
                Map<Method, Subscribe> annotatedMethods = MethodIntrospector.selectMethods(iDomainEventHandler.getClass(),
                    (MethodIntrospector.MetadataLookup<Subscribe>)method -> {
                        if ("handleDomainEvent".equals(method.getName()) && Objects.deepEquals(method.getParameterTypes(), parameterTypes)) {
                            return AnnotatedElementUtils.getMergedAnnotation(method, Subscribe.class);
                        }
                        return null;
                    });
                if (MapUtils.isEmpty(annotatedMethods)) {
                    throw new Error("domain event handler method miss annotation:@Subscribe! class:" + iDomainEventHandler.getClass());
                }
                this.eventBus.register(iDomainEventHandler);
            }
        }
    }
}
