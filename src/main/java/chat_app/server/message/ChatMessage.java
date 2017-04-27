package chat_app.server.message;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * Chat-Message for communication between {@link chat_app.client.ClientEntity Client-Thread} and
 * {@link chat_app.server.ConnectedClient Connected-Client-Thread} from server.
 */
public class ChatMessage {

    @NotNull
    private MessageType type;

    @NotNull
    private String message;

    /**
     * For JSON.
     */
    public ChatMessage() {
        this.type = MessageType.MESSAGE;
        this.message = "";
    }

    /**
     * Constructor.
     *
     * @param type    message type. Not null.
     * @param message message. Not null.
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
