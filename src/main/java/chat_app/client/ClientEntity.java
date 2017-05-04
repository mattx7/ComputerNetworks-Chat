package chat_app.client;

import chat_app.message.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Is the chat-client for the user. Connects to the server and can send messages.
 *
 * @see ServerListener
 */
class ClientEntity {
    private static final Logger LOG = Logger.getLogger(ClientEntity.class);

    ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;

    /**
     * Holds JSON-mapper for transfer.
     */
    private final ObjectMapper mapper = new ObjectMapper();

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
            socket = new Socket(serverAddress, port);
            LOG.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
        } catch (final IOException e) {
            throw new ServerNotFoundException();
        }

        // Create Streams
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            LOG.error("Error:", e);
            disconnect();
            return;
        }

        // Receives messages from server
        new ServerListener(this).start();

        // Send our username to the server
        try {
            outputStream.writeObject(username);
        } catch (IOException eIO) {
            disconnect();
        }

    }

    /**
     * To send message to serverAddress.
     *
     * @param message Not null.
     */
    void sendMessage(@NotNull ChatMessage message) {
        try {
            String jsonString = mapper.writeValueAsString(message);
            outputStream.writeObject(jsonString);
        } catch (IOException e) {
            LOG.error("Error:", e);
        }
    }

    /**
     * Close connection and streams.
     */
    @SuppressWarnings("Duplicates")
    void disconnect() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (Exception ignore) {
            // IGNORED
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
}
