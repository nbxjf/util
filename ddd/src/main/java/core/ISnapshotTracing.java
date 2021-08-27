package core;

import utils.deepclone.DeepCloneUtil;

/**
 * 基于快照方案的变更追踪
 *
 * @param <T> 被追踪的数据
 * @param <D> 变化的差异数据
 * @author Jeff_xu
 * @date 2021/1/18
 */
public interface ISnapshotTracing<T, D> extends IChangeTraceable<D> {
    /**
     * 开启追踪（创建当前数据快照）
     */
    @Override
    default void attach() {
        if (getLatestSnapshot() != null) {
            throw new IllegalStateException("快照已创建，务必先持久化后重建快照，否则会导致更新丢失！");
        }
        setSnapshot(createSnapshot());
    }

    /**
     * 终止追踪（清理数据快照）
     */
    @Override
    default void detach() {
        this.setSnapshot(null);
    }

    /**
     * 创建数据快照
     */
    default T createSnapshot() {
        //清空已有快照
        setSnapshot(null);
        //深度拷贝
        return DeepCloneUtil.deepClone(getCurrentData());
    }

    /**
     * 获取当前数据
     */
    T getCurrentData();

    /**
     * 保存快照
     */
    void setSnapshot(T snapShot);

    /**
     * 获取当前保存的最新的快照
     */
    T getLatestSnapshot();

    /**
     * 修改的数据
     */
    @Override
    D getChangeInfo();
}
