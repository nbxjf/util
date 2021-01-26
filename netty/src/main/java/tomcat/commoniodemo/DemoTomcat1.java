package tomcat.commoniodemo;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jeff_xu on 2020/11/6.
 *
 * @author Jeff_xu
 */
public class DemoTomcat1 {

    private static final int PORT = 8080;
    private ServerSocket serverSocket;

    public void start() {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Tomcat 已启动,启动端口是:" + PORT);
            while (true) {
                // 基于传统 socket
                Socket accept = serverSocket.accept();
                InputStream inputStream = accept.getInputStream();
                Request request = new Request(inputStream);

                OutputStream outputStream = accept.getOutputStream();
                outputStream.write(new StringBuilder().append("HTTP/1.1 200 OK\n")
                    .append("Content-Type: text/html;\n")
                    .append("\r\n")
                    // 返回的结果信息
                    .append("45678979879079807890")
                    .toString().getBytes());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        new DemoTomcat1().start();
    }

}
