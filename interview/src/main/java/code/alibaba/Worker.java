package code.alibaba;

import java.util.List;

/**
 * Created by Jeff_xu on 2021/3/23.
 * 员工
 *
 * @author Jeff_xu
 */
public class Worker extends Thread {

    private int workerId;
    /**
     * 工人状态
     * 0 待工作
     * 1 工作中
     * -1 已清退
     */
    private volatile int workState;

    private long startWorkTime;

    private long stopWorkTime;

    public Worker(int workerId) {
        this.workerId = workerId;
    }

    @Override
    public void run() {
        List<Machine> machineList = Platform.machineList;
        for (Machine machine : machineList) {
            if (!machine.tryAcquire(this)) {
                machine.addWaiter(this);
                machine.acquireQueued(this);
            } else {
                break;
            }
        }
        startWork();

        while (workState == 1) {
            // 工作中，省略
        }
    }

    public int getWorkerId() {
        return workerId;
    }

    public int getWorkState() {
        return workState;
    }

    public void startWork() {
        this.workState = 1;
        this.startWorkTime = System.currentTimeMillis();
    }

    /**
     * 终止工作
     */
    public void stopWork() {
        this.workState = 0;
        this.stopWorkTime = System.currentTimeMillis();
    }

    /**
     * 工作时长
     */
    public long getWorkTime() {
        long end = stopWorkTime > 0 ? stopWorkTime : System.currentTimeMillis();
        return end - startWorkTime;
    }

}
