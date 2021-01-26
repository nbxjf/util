package nio;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Jeff_xu on 2020/11/4.
 *
 * @author Jeff_xu
 */
public class FileNioTest {

    public static final byte[] message = {12,23,56,78,90,45,67,90};

    public static void main(String[] args) throws Exception {
        // 创建文件流
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/Jeff/test.txt");

        // 获取 channel
        FileChannel channel = fileOutputStream.getChannel();

        // 创建一个缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 向缓冲区中写数据
        for (int i = 0; i < message.length; i++) {
            buffer.put(message[i]);
        }

        // 反转锁定缓冲区
        buffer.flip();

        // 使用通道写数据
        channel.write(buffer);
        // 关闭通道
        channel.close();

    }
}
