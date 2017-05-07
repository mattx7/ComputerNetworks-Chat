package chat_app.transfer_object;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * Chat-Message for communication between {@link chat_app.client.ClientEntity Client-Thread} and
 * {@link chat_app.server.ConnectedClient Connected-Client-Thread} format server.
 */
public class Message {

    @NotNull
    private MessageType type;

    @NotNull
    private String payload;

    /**
     * Constructor for JSON.
     */
    @SuppressWarnings("unused")
    public Message() {
        this.type = MessageType.MESSAGE;
        this.payload = "";
    }

    /**
     * Constructor for convenience.
     *
     * @param type Not null.
     */
    public Message(@NotNull final MessageType type) {
        this.type = type;
        this.payload = "";
    }

    /**
     * Constructor for convenience.
     *
     * @param payload Not null.
     */
    public Message(@NotNull final String payload) {
        this.type = MessageType.MESSAGE;
        this.payload = payload;
    }

    /**
     * Full Constructor.
     *
     * @param type    Not null.
     * @param payload Not null.
     */
    public Message(@NotNull final MessageType type, @NotNull final String payload) {
        Preconditions.checkNotNull(type, "type must not be null.");
        Preconditions.checkNotNull(payload, "payload must not be null.");

        this.type = type;
        this.payload = payload;
    }

    /**
     * @return type.
     */
    @NotNull
    public MessageType getType() {
        return type;
    }

    /**
     * @return payload.
     */
    @NotNull
    public String getPayload() {
        return payload;
    }

}
