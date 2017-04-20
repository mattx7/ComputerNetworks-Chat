package chat_app.message;

import java.io.Serializable;

/**
 * Message for ChatClient
 */
public class Message implements Serializable {

    protected static final long serialVersionUID = 1112122200L; // TODO Why?

    private MessageType type;
    private String message;

    public Message(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

}
