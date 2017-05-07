package chat_app.client;

import chat_app.transfer_object.Message;
import chat_app.utility.Connection;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Is the chat-client for the user. Connects to the server and can send messages.
 *
 * @see ServerListener
 */
class ClientEntity {
    private static final Logger LOG = Logger.getLogger(ClientEntity.class);

    /**
     * Connection.
     */
    Connection connection;

    /**
     * Name of the user.
     */
    private String username;

    /**
     * Constructor.
     *
     * @param username Not null.
     */
    ClientEntity(@NotNull final String username) {
        Preconditions.checkNotNull(username, "username must not be null.");

        this.username = username;
    }

    /**
     * Starts the connection to the server
     *
     * @param address IP or URL to the server. Not null.
     * @param port    Port number. Not null.
     * @throws ServerNotFoundException If connection gets refused.
     */
    void connect(@NotNull final String address,
                 @NotNull final Integer port) throws ServerNotFoundException {
        Preconditions.checkNotNull(address, "address must not be null.");
        Preconditions.checkNotNull(port, "port must not be null.");

        // Try to connect
        try {
            connection = Connection.to(address, port);
            LOG.info("Connection accepted " + connection.getServerAddress() + ":" + connection.getPort());
        } catch (final IOException e) {
            connection.kill();
            throw new ServerNotFoundException();
        }

        // Receives messages from server
        new ServerListener(this).start();

        // Send username to the server
        try {
            connection.send(username);
        } catch (IOException eIO) {
            connection.kill();
        }
    }

    /**
     * To send transfer_object to serverAddress.
     *
     * @param message Not null.
     */
    void sendMessage(@NotNull final Message message) {
        Preconditions.checkNotNull(message, "message must not be null.");

        try {
            connection.send(message);
        } catch (IOException e) {
            LOG.error("Error:", e);
        }
    }

    /**
     * To display in terminal
     *
     * @param message Not null.
     */
    void display(@NotNull final String message) {
        Preconditions.checkNotNull(message, "message must not be null.");

        System.out.println(message);
    }

    /**
     * To display in terminal
     *
     * @param message Not null.
     */
    void display(@NotNull final Message message) {
        Preconditions.checkNotNull(message, "message must not be null.");

        System.out.println(message.getPayload());
    }
}
