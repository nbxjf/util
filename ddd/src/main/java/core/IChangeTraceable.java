package core;

/**
 * 变化追踪
 *
 * @param <D> 变更的数据集
 * @author Jeff_xu
 * @date 2021/1/19
 */
public interface IChangeTraceable<D> {
    /**
     * 开始跟踪变化
     */
    void attach();

    /**
     * 停止跟踪变化
     */
    void detach();

    /**
     * 获取自开启追踪之后变更的信息
     */
    D getChangeInfo();
}
