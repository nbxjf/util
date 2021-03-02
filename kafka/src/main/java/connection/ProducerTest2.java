package connection;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Created by Jeff_xu on 2021/2/27.
 *
 * @author Jeff_xu
 */
public class ProducerTest2 {

    public static void main(String[] args) throws InterruptedException {

        Properties properties = new Properties();

        // 使用常量定义，防止出错
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties, new StringSerializer(), new StringSerializer());

        Future<RecordMetadata> result = kafkaProducer.send(new ProducerRecord<>("test", "Hi kafka ,this is idea"));
        while (!result.isDone()) {
            System.out.println("发送中...");
            Thread.sleep(10);
        }
        System.out.println("发送成功...");
    }
}
