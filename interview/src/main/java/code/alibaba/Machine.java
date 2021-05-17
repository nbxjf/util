package code.alibaba;

import java.lang.reflect.Field;
import java.util.Queue;

import sun.misc.Unsafe;

/**
 * Created by Jeff_xu on 2021/3/23.
 * 主机
 *
 * @author Jeff_xu
 */
public class Machine {
    /**
     * 主机id
     */
    private int machineId;
    /**
     * 0 空闲
     * >1 繁忙
     * -1 待删除
     */
    private volatile int machineState;
    /**
     * 当前持有主机的员工
     */
    private volatile Worker currentWorker;

    /**
     * 等待队列
     */
    private Queue<Worker> waiterList;

    public Machine(int machineId) {
        this.machineId = machineId;
    }

    private static final Unsafe unsafe;

    static {
        try {
            // 通过反射获取unsafe
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public boolean acquireQueued(Worker worker) {
        for (; ; ) {
            if (machineState == 0) {
                while (!waiterList.isEmpty()) {
                    Worker topWorker = waiterList.peek();
                    if (topWorker.getWorkState() != 0) {
                        waiterList.poll();
                    }
                    if (topWorker == worker && topWorker.getWorkState() == 0) {
                        return compareAndSetState(0, 1);
                    }
                }
            } else {
                // 自旋等待
            }
        }
    }

    public boolean stopMachine() {
        for (; ; ) {
            if (getMachineState() == 0 && compareAndSetState(0, -1)) {
                return true;
            }
        }

    }

    public void addWaiter(Worker worker) {
        waiterList.add(worker);
    }

    public boolean tryAcquire(Worker worker) {
        if (machineState == 0 && compareAndSetState(0, 1)) {
            setCurrentWorker(worker);
            return true;
        } else if (currentWorker == worker) {
            // 当前线程就是持有的员工
            return true;
        }
        return false;
    }

    public int getMachineId() {
        return machineId;
    }

    public int getMachineState() {
        return machineState;
    }

    private void setCurrentWorker(Worker worker) {
        this.currentWorker = worker;
    }

    private boolean compareAndSetState(int expect, int update) {
        try {
            long machineStateOffset = unsafe.objectFieldOffset(Machine.class.getDeclaredField("machineState"));
            return unsafe.compareAndSwapObject(this, machineStateOffset, expect, update);
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

}
