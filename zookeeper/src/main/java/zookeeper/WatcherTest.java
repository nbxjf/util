package zookeeper;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.CollectionUtils;

/**
 * Created by Jeff_xu on 2020/12/2.
 * watcher demo
 *
 * @author Jeff_xu
 */
@Slf4j
public class WatcherTest implements Watcher, AsyncCallback.StatCallback {

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    private ZooKeeper zk;

    private static Object object = new Object();

    public void connect(String connectAddres, int sessionTimeOut) {
        try {
            zk = new ZooKeeper(connectAddres, sessionTimeOut, this);
            countDownLatch.await();
            if (zk.exists("/perf_test", false) == null) {
                zk.create("/perf_test", "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            zk.exists("/perf_test", this);
            synchronized (object) {
                object.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        Event.KeeperState keeperState = watchedEvent.getState();
        Event.EventType eventType = watchedEvent.getType();
        String path = watchedEvent.getPath();
        if (Event.KeeperState.SyncConnected == keeperState) {
            if (Event.EventType.None == eventType) {
                countDownLatch.countDown();
                System.out.println("zk 建立连接成功!");
            } else if (Event.EventType.NodeCreated == eventType) {
                System.out.println("事件通知,新增node节点" + path);

            } else if (Event.EventType.NodeDataChanged == eventType) {
                System.out.println("事件通知,当前node节点" + path + "被修改....");

            } else if (Event.EventType.NodeDeleted == eventType) {
                System.out.println("事件通知,当前node节点" + path + "被删除....");

            } else if (Event.EventType.NodeChildrenChanged == eventType) {
                System.out.println("事件通知,当前node节点的子节点列表发生改变：" + path);

            }
        }
        try {
            zk.exists("/perf_test", this);
            if (path != null) {
                List<String> children = zk.getChildren(path, this);
                if (!CollectionUtils.isEmpty(children)) {
                    zk.getChildren(path, this, this::processResult1, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processResult1(int rc, String path1, Object ctx, List<String> children) {
        try {
            for (String child : children) {
                zk.getData(child, this, this::processResult2, null);
            }
            zk.getChildren(path1, this, this::processResult1, null);
        } catch (Exception e) {
            log.error("processResult1 error", e);
        }
    }

    public void processResult2(int rc, String path, Object ctx, byte data[],
                               Stat stat) {
        try {
            log.info("path:{},data:{}", path, data);
            zk.getChildren(path, this, this::processResult1, null);
        } catch (Exception e) {
            log.error("processResult2 error", e);
        }

    }

    public static void main(String[] args) throws InterruptedException {
        WatcherTest watcherTest = new WatcherTest();
        watcherTest.connect("127.0.0.1:2181", 1000);
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        System.out.println("rc:" + rc);
        System.out.println("path:" + path);
        System.out.println("ctx:" + ctx);
        System.out.println("stat:" + stat);
        switch (KeeperException.Code.get(rc)) {
            case OK:
                exists = true;
                break;
            case NONODE:
                exists = false;
                break;
            case SESSIONEXPIRED:
            case NOAUTH:
                return;
            default:
                zk.exists(path, true, this, stat);
                return;
        }
        if (exists) {
            try {
                String val = new String(zk.getData(path, false, null));
                System.out.println("数据信息：" + val);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}