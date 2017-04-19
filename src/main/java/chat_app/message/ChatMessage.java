package chat_app.message;

import java.io.Serializable;

/**
 * Message for ChatClient
 */
public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L; // TODO Why?

    private ChatMessageType type;
    private String message;

    public ChatMessage(ChatMessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    public ChatMessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

}
