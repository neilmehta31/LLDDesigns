import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

class Message{
    private final String content;
    private final String producerId;

    public Message(String content, String producerId) {
        this.content = content;
        this.producerId = producerId;
    }

    public String getProducerId() {
        return producerId;
    }

    public String getContent() {
        return content;
    }
}

class Topic{
    private final String name;
    private final BlockingQueue<Message> messages;
    private final List<Consumer> subscribers;

    public Topic(String name) {
        this.name = name;
        this.messages = new LinkedBlockingQueue<>();
        this.subscribers = new ArrayList<>();
    }

    public void subscribe(Consumer consumer){
        subscribers.add(consumer);
    }

    public void addMessage(Message message){
        boolean isOffer = messages.offer(message);
        if(isOffer) {
            notifySubscribers();
        }
    }

    private void notifySubscribers() {
        try {
            Message message = takeMessage();
            for(Consumer subscriber: subscribers){
                subscriber.onMessageAvailable(message, this);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    private Message takeMessage() throws InterruptedException {
        return messages.take();
    }
}

class Consumer implements Runnable {
    private final String id;
    private final QueueSystem queueSystem;

    public Consumer(String id, QueueSystem queueSystem) {
        this.id = id;
        this.queueSystem = queueSystem;
    }

    public void subscribe(String topicName) {
        queueSystem.subscribeConsumer(topicName, this);
    }

    public void onMessageAvailable(Message message, Topic topic){
        System.out.println(id + " received " + message.getContent() + " on topic " + topic.getName());
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            try{
                Thread.sleep(100);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }
}

class Producer{
    private final String id;
    private final QueueSystem queueSystem;

    public Producer(String id, QueueSystem queueSystem) {
        this.id = id;
        this.queueSystem = queueSystem;
    }

    public void publish(String topicName, String messageContent){
        Message message = new Message(messageContent, id);
        queueSystem.publishMessage(topicName, message);
    }
}

class QueueSystem {
    private ConcurrentHashMap<String, Topic> topics;
    private ExecutorService executorService;

    public QueueSystem() {
        this.topics = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
    }

    public void createTopic(String topicName){
        topics.putIfAbsent(topicName, new Topic(topicName));
    }

    public void publishMessage(String topicName, Message message){
        Topic topic = topics.get(topicName);
        if(topic!= null){
            topic.addMessage(message);
        }
    }

    public void subscribeConsumer(String topicName, Consumer consumer){
        Topic topic = topics.get(topicName);
        if (topic!= null){
            topic.subscribe(consumer);
            executorService.submit(consumer);
        }
    }

    public void shutdown(){
        executorService.shutdown();
    }
}

public class KafkaLLD {
    public static void main(String[] args) {
        QueueSystem qs = new QueueSystem();
        qs.createTopic("topic1");
        qs.createTopic("topic2");

        Producer producer1 = new Producer("producer1", qs);
        Producer producer2 = new Producer("producer2", qs);

        Consumer consumer1 = new Consumer("consumer1", qs);
        Consumer consumer2 = new Consumer("consumer2", qs);
        Consumer consumer3 = new Consumer("consumer3", qs);
        Consumer consumer4 = new Consumer("consumer4", qs);
        Consumer consumer5 = new Consumer("consumer5", qs);

        consumer1.subscribe("topic1");
        consumer2.subscribe("topic1");
        consumer3.subscribe("topic1");
        consumer4.subscribe("topic1");
        consumer5.subscribe("topic1");

        consumer1.subscribe("topic2");
        consumer3.subscribe("topic2");
        consumer4.subscribe("topic2");

        producer1.publish("topic1", "Message 1");
        producer1.publish("topic1", "Message 2");
        producer2.publish("topic1", "Message 3");
        producer1.publish("topic2", "Message 4");
        producer2.publish("topic2", "Message 5");

        // Allow some time for message processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        qs.shutdown();

    }
}












































