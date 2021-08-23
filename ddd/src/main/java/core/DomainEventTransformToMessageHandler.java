package core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.GenericTypeResolver;
import message.KafkaJsonMessageService;

/**
 * 当出现subscriber exceptions, 将领域事件转化为消息, 进行重试
 * <p>
 * 注意：领域事件要支持fastjson序列化反序列化需要具备无参构造函数以及set函数
 * <p>[
 * 配置说明：spring xml中添加如下配置
 * <pre>{@code
 * <bean id="exceptionHandler" class="DomainEventTransformToMessageHandler">
 *         <constructor-arg name="kafkaJsonMessageService" ref="kafkaJsonMessageService"/>
 *         <constructor-arg name="retryQueueName" value="Order-domainEventRetryQueue"/>
 *         <constructor-arg name="groupName" value="order"/>
 *         <constructor-arg name="retryThreadCount" value="5"/>
 * </bean>
 * }</pre>
 *
 * @author Jeff_xu
 * @date 2021/2/24
 */
@Slf4j
public class DomainEventTransformToMessageHandler implements ApplicationListener<ContextRefreshedEvent>, SubscriberExceptionHandler {
    private KafkaJsonMessageService kafkaJsonMessageService;
    private String retryQueueName;
    private String groupName;
    private int retryThreadCount;
    private Map<Class<?>, List<IDomainEventHandler<IDomainEvent>>> domainEventHandlerMap = Maps.newHashMap();

    public DomainEventTransformToMessageHandler(KafkaJsonMessageService kafkaJsonMessageService,
                                                String groupName,
                                                String retryQueueName,
                                                int retryThreadCount) {
        this.retryQueueName = retryQueueName;
        this.groupName = groupName;
        this.kafkaJsonMessageService = kafkaJsonMessageService;
        this.retryThreadCount = retryThreadCount;
    }

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        Object event = context.getEvent();
        if (event instanceof IDomainEvent) {
            IDomainEvent domainEvent = (IDomainEvent) event;
            //3s后重试
            this.kafkaJsonMessageService.enqueue(this.retryQueueName, domainEvent, 3);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, IDomainEventHandler> domainEventHandlerMap = event.getApplicationContext().getBeansOfType(IDomainEventHandler.class);
        if (MapUtils.isEmpty(domainEventHandlerMap)) {
            return;
        }
        this.domainEventHandlerMap = domainEventHandlerMap.values().stream()
                .map(iDomainEventHandler -> (IDomainEventHandler<IDomainEvent>) iDomainEventHandler)
                .collect(Collectors
                        .groupingBy(iDomainEventHandler -> GenericTypeResolver.resolveTypeArgument(iDomainEventHandler.getClass(), IDomainEventHandler.class),
                                Collectors.toList()));
        //事件消息监听
//        this.kafkaJsonMessageService.listen(this.retryQueueName, this.groupName, this.retryThreadCount, IDomainEvent.class, (domainEvent) -> {
//            List<IDomainEventHandler<IDomainEvent>> domainEventHandlers = this.domainEventHandlerMap.get(domainEvent.getClass());
//            if (CollectionUtils.isEmpty(domainEventHandlers)) {
//                log.error("can not find any domainEventHandler for event:{}", domainEvent);
//            } else {
//                for (IDomainEventHandler<IDomainEvent> domainEventHandler : domainEventHandlers) {
//                    domainEventHandler.handleDomainEvent(domainEvent);
//                }
//            }
//        });
    }
}
