package core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import core.DomainEventTransactionalPublisher.IDomainEventDispatcher.PerThreadQueuedDispatcher;
import core.DomainEventTransactionalPublisher.IDomainEventSubscriber.DefaultSubscriber;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.GenericTypeResolver;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 领域事件发布器，发布并处理聚合根履行职责时生成的领域事件，用来做跨域通信，将变化通过事件模型传递给其他域。
 * <p>
 * 事件发布或者执行都在当前业务线程中进行按照事件发布顺序执行（广度优先策略），执行异常会抛出至当前业务线程，在事务块中使用，事件处理出现异常事务回滚。
 * <p>
 * 实现细节：domainEventHandlerMap中托管了当前系统内的领域事件监听器{@link IDomainEventHandler}，根据聚合根中的事件类型，将相应的{@link IDomainEvent}发送给对应的domainEventHandler并执行。
 * <p>
 * 使用说明：spring xml中添加如下配置
 * <pre>{@code
 * <bean id="domainEventPublisher" class="DomainEventTransactionalPublisher"/>
 * }</pre>
 * <p>
 *
 * @author Jeff_xu
 * @date 2021/1/8
 * @see DomainEventBus
 */
@Slf4j
public class DomainEventTransactionalPublisher implements ApplicationListener<ContextRefreshedEvent>, IDomainEventPublisher {
    @Setter
    private Map<Class<?>, List<IDomainEventSubscriber>> subscriberMap = Maps.newHashMap();

    private IDomainEventDispatcher dispatcher = new PerThreadQueuedDispatcher();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, IDomainEventHandler> domainEventHandlerMap = event.getApplicationContext().getBeansOfType(IDomainEventHandler.class);
        if (MapUtils.isNotEmpty(domainEventHandlerMap)) {
            this.subscriberMap = domainEventHandlerMap.values().stream()
                .map(iDomainEventHandler -> (IDomainEventHandler<IDomainEvent>)iDomainEventHandler)
                .collect(Collectors
                    .groupingBy(iDomainEventHandler -> GenericTypeResolver.resolveTypeArgument(iDomainEventHandler.getClass(), IDomainEventHandler.class),
                        Collectors.mapping(DefaultSubscriber::new, Collectors.toList())));
        }
    }

    private void dispatch(IDomainEvent domainEvent) {
        List<IDomainEventSubscriber> subscribers = subscriberMap.get(domainEvent.getClass());
        if (CollectionUtils.isEmpty(subscribers)) {
            throw new RuntimeException("can not find any domainEventHandler for event:" + domainEvent.getClass());
        } else {
            dispatcher.dispatch(domainEvent, subscribers.iterator());
        }
    }

    /**
     * 立即发送领域事件
     */
    @Override
    public void publish(IDomainEventProducer producer) {
        if (CollectionUtils.isNotEmpty(producer.getDomainEvents())) {
            try {
                for (IDomainEvent domainEvent : producer.getDomainEvents()) {
                    dispatch(domainEvent);
                }
            } finally {
                producer.clearDomainEvents();
            }
        }
    }

    /**
     * 订阅者
     */
    public static abstract class IDomainEventSubscriber {
        @Getter
        private IDomainEventHandler<IDomainEvent> target;

        public IDomainEventSubscriber(IDomainEventHandler<IDomainEvent> target) {
            this.target = target;
        }

        final void dispatchDomainEvent(final IDomainEvent event) {
            target.handleDomainEvent(event);
        }

        public static final class DefaultSubscriber extends IDomainEventSubscriber {
            public DefaultSubscriber(IDomainEventHandler<IDomainEvent> target) {
                super(target);
            }
        }
    }

    /**
     * 事件派发器
     */
    interface IDomainEventDispatcher {
        void dispatch(IDomainEvent event, Iterator<IDomainEventSubscriber> subscribers);

        final class PerThreadQueuedDispatcher implements IDomainEventDispatcher {

            /**
             * Per-thread queue of events to dispatch.
             */
            private final ThreadLocal<Queue<Event>> queue = ThreadLocal.withInitial(Queues::newArrayDeque);

            /**
             * Per-thread dispatch state, used to avoid reentrant event dispatching.
             */
            private final ThreadLocal<Boolean> dispatching = ThreadLocal.withInitial(() -> false);

            @Override
            public void dispatch(IDomainEvent event, Iterator<IDomainEventSubscriber> subscribers) {
                checkNotNull(event);
                checkNotNull(subscribers);
                Queue<Event> queueForThread = queue.get();
                queueForThread.offer(new Event(event, subscribers));

                if (!dispatching.get()) {
                    dispatching.set(true);
                    try {
                        Event nextEvent;
                        while ((nextEvent = queueForThread.poll()) != null) {
                            while (nextEvent.subscribers.hasNext()) {
                                nextEvent.subscribers.next().dispatchDomainEvent(nextEvent.event);
                            }
                        }
                    } finally {
                        dispatching.remove();
                        queue.remove();
                    }
                }
            }

            private static final class Event {
                private final IDomainEvent event;
                private final Iterator<IDomainEventSubscriber> subscribers;

                private Event(IDomainEvent event, Iterator<IDomainEventSubscriber> subscribers) {
                    this.event = event;
                    this.subscribers = subscribers;
                }
            }
        }
    }
}
