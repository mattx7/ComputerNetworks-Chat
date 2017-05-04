package chat_app.client;

import chat_app.message.ChatMessage;
import chat_app.utility.Connection;
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
     * IP or address to the server
     */
    private String serverAddress;

    /**
     * Name of the user.
     */
    private String username;

    /**
     * Holds the port.
     */
    private int port;

    /**
     * Constructor.
     *
     * @param serverAddress Not null.
     * @param username      Not null.
     * @param port          Not null.
     */
    ClientEntity(@NotNull String serverAddress, @NotNull String username, int port) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.port = port;
    }

    /**
     * Starts the connection to the server
     *
     * @throws ServerNotFoundException If connection gets refused.
     */
    void connect() throws ServerNotFoundException {

        // Connect
        try {
            connection = Connection.to(serverAddress, port);
            LOG.info("Connection accepted " + connection.getServerAddress() + ":" + connection.getPort());
        } catch (final IOException e) {
            connection.kill();
            throw new ServerNotFoundException();
        }

        // Receives messages format server
        new ServerListener(this).start();

        // Send our username to the server
        try {
            connection.send(username);
        } catch (IOException eIO) {
            connection.kill();
        }

    }

    /**
     * To send message to serverAddress.
     *
     * @param message Not null.
     */
    void sendMessage(@NotNull ChatMessage message) {
        try {
            connection.send(message);
        } catch (IOException e) {
            LOG.error("Error:", e);
        }
    }

    /**
     * To display in terminal
     *
     * @param message  Not null.
     */
    void display(@NotNull String message) {
        System.out.println(message);
    }

    /**
     * To display in terminal
     *
     * @param chatMessage  Not null.
     */
    void display(@NotNull ChatMessage chatMessage) {
        System.out.println(chatMessage.getMessage());
    }
}
