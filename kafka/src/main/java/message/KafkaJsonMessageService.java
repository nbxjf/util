package message;

public class KafkaJsonMessageService {

    public <T> void enqueue(String topicName, String key, T message) {

    }

    public <T> void enqueue(String topicName, T message, int delayTime) {

    }


//    public <T> void listen(String topicName,
//                           String groupName,
//                           int threadCount,
//                           Class<T> messageType,
//                           KafkaMessageHandler.Sync<T> handler) {
//
//    }
}
