package tomcat.commoniodemo;

import java.io.InputStream;

/**
 * Created by Jeff_xu on 2020/11/6.
 *
 * @author Jeff_xu
 */
public class Request {

    private String method;
    private String url;

    public Request(InputStream in) {
        try {
            String content = "";
            byte[] request = new byte[1024];
            int len = 0;
            if ((len = in.read(request)) > 0) {
                content = new String(request, 0, len);
            }
            System.out.println(content);
            // 得到 http的请求内容如下：
            //            GET / HTTP/1.1
            //            Host: 127.0.0.1:8080
            //            Connection: keep-alive
            //            Upgrade-Insecure-Requests: 1
            //            User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36
            //            Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
            //Sec-Fetch-Site: none
            //Sec-Fetch-Mode: navigate
            //Sec-Fetch-User: ?1
            //Sec-Fetch-Dest: document
            //Accept-Encoding: gzip, deflate, br
            //Accept-Language: zh-CN,zh;q=0.9,en;q=0.8

            // 解析得到 method 以及 url
            this.method = "";
            this.url = "";

        } catch (Exception e) {

        }
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }
}
