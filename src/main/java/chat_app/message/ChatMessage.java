package chat_app.message;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * ChatMessage for ChatClient
 */
public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L; // TODO Why?

    @NotNull
    private MessageType type;

    @NotNull
    private String message;

    public ChatMessage(@NotNull MessageType type, @NotNull String message) {
        Preconditions.checkNotNull(type, "type must not be null.");
        Preconditions.checkNotNull(message, "message must not be null.");

        this.type = type;
        this.message = message;
    }

    @NotNull
    public MessageType getType() {
        return type;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

}
