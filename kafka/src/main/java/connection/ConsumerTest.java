package connection;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Created by Jeff_xu on 2021/2/23.
 * kafka消息生产者测试
 *
 * @author Jeff_xu
 */
public class ConsumerTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        // 置顶group群组的名称（即消费群组）
        properties.put("group.id", "consumerTest");
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties, new StringDeserializer(), new StringDeserializer());

        kafkaConsumer.subscribe(Collections.singletonList("test"));

        while (true) {
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.of(1, SECONDS));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                System.out.println("key" + consumerRecord.key());
                System.out.println("value" + consumerRecord.value());
                System.out.println("topic:" + consumerRecord.topic());
                System.out.println("partition" + consumerRecord.partition());
                System.out.println("============================");
            }
        }
    }
}
