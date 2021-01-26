package zookeeper;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by Jeff_xu on 2020/12/2.
 *
 * @author Jeff_xu
 */
@Slf4j
public class CreateNodeTest {

    public void create(long mills) {
        String rootPath = "/perf_test";

        byte[] bytes = "1".getBytes(StandardCharsets.UTF_8);

        int i = 1;
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper("127.0.0.1:2181", 10000, null);
            if (zk.exists(rootPath, false) == null) {
                zk.create(rootPath, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            while (i <= 2001) {
                try {
                    String path = rootPath + "/sub" + i;
                    if (i <= 2001 - 1000) {
                        zk.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                        System.out.println("Create node:{}" + path);
                        Thread.sleep(mills);
                    }
                    if (i > 1000) {
                        zk.delete(rootPath + "/sub" + (i - 1000), -1);
                        System.out.println("Delete node:{}" + rootPath + "/sub" + (i - 1000));
                        Thread.sleep(mills);
                    }

                } catch (Exception e) {
                    System.out.println("set data failed" + e);
                }
                i++;
            }
            zk.delete(rootPath, -1);
        } catch (Exception e) {
            System.out.println("connect to zk failed" + e);
        } finally {
            try {
                if (zk != null) {
                    zk.close();
                }
            } catch (InterruptedException e) {
                System.out.println("close zk connection failed" + e);

            }
        }
    }

    public static void main(String[] args) {
        CreateNodeTest createNodeTest = new CreateNodeTest();
        createNodeTest.create(5);
    }
}
