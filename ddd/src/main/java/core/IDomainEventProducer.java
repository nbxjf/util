package core;

import java.util.List;

/**
 * 事件生产者（暂存领域事件）
 *
 * @author Jeff_xu
 * @date 2021/2/19
 */
public interface IDomainEventProducer {
    /**
     * 事件集合
     */
    List<IDomainEvent> getDomainEvents();

    /**
     * 删除事件
     */
    default void removeDomainEvent(IDomainEvent eventItem) {
        getDomainEvents().remove(eventItem);
    }

    /**
     * 新增事件
     */
    default void addDomainEvent(IDomainEvent eventItem) {
        getDomainEvents().add(eventItem);
    }

    /**
     * 清理事件
     */
    default void clearDomainEvents() {
        getDomainEvents().clear();
    }
}
