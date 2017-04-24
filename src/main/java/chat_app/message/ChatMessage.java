package chat_app.message;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * ChatMessage for ChatClient
 */
public class ChatMessage implements Serializable {

    @NotNull
    private MessageType type;

    @NotNull
    private String message;

    /**
     * Constructor
     *
     * @param type    message type
     * @param message message
     */
    public ChatMessage(@NotNull MessageType type, @NotNull String message) {
        Preconditions.checkNotNull(type, "type must not be null.");
        Preconditions.checkNotNull(message, "message must not be null.");

        this.type = type;
        this.message = message;
    }

    /**
     * @return type.
     */
    @NotNull
    public MessageType getType() {
        return type;
    }

    /**
     * @return message.
     */
    @NotNull
    public String getMessage() {
        return message;
    }

}
