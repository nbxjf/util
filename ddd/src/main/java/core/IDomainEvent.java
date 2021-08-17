package core;

import java.io.Serializable;

/**
 * 领域事件
 * <p>
 * 使用场景一： 在事务提交前同步响应领域事件，外部服务调用失败，回滚当前事务；
 * <p>
 * 使用场景二： 在事务提交后同步响应领域事件，优先提交当前事务，不受外部服务调用结果影响；
 */
public interface IDomainEvent extends Serializable {
}