package HC.Communication;

import java.io.Serializable;
import HC.Enumerates.MessageTopic;

public class Message implements Serializable {
    private MessageTopic topic;

    public Message(MessageTopic topic) {
        this.topic = topic;
    }

    public void setTopic(MessageTopic topic) {
        this.topic = topic;
    }

    public MessageTopic getTopic() {
        return this.topic;
    }
}