package code.alibaba;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeff_xu on 2021/3/23.
 * 平台
 *
 * @author Jeff_xu
 */
public class Platform {

    public static List<Worker> workerList = new ArrayList<>();

    public static List<Machine> machineList = new ArrayList<>();

    /**
     * 添加主机
     *
     * @param machineId 主机id
     */
    public static void addMachine(int machineId) {
        machineList.add(new Machine(machineId));
    }

    /**
     * 减少主机
     */
    public void delMachine(int machineId) {
        Machine machine = machineList.stream().filter(o -> o.getMachineId() == machineId).findAny().orElseThrow(() -> new RuntimeException("工人不存在"));
        if (machine.stopMachine()) {
            machineList.remove(machine);
        }
    }

    /**
     * 新增员工
     *
     * @param workerId 员工id
     */
    public static void addWorker(int workerId) {
        Worker worker = new Worker(workerId);
        workerList.add(worker);
        worker.start();
    }

    /**
     * 清退员工
     *
     * @param workerId 员工id
     */
    public void delWorker(int workerId) {
        Worker worker = workerList.stream().filter(o -> o.getWorkerId() == workerId).findAny().orElseThrow(() -> new RuntimeException("工人不存在"));
        worker.stopWork();
        workerList.remove(worker);
    }

    /**
     * 获取员工的工作时长
     *
     * @param workerId 员工id
     */
    public long getWorkingHour(int workerId) {
        return 0;
    }
}
