package chat_app.utility;

import chat_app.message.ChatMessage;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Holds all useful stuff for connection.
 */
public class Connection {

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private String serverAddress;
    private Integer port;

    private Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream()); // Must be declared before Input
        this.inputStream = new ObjectInputStream(socket.getInputStream());

        this.serverAddress = socket.getInetAddress().toString();
        this.port = socket.getPort();
    }

    /**
     * Creates new instance.
     */
    public synchronized static Connection to(@NotNull Socket socket) throws IOException {
        Preconditions.checkNotNull(socket, "socket must not be null.");

        return new Connection(socket);
    }

    /**
     * Creates new instance.
     */
    public synchronized static Connection to(@NotNull String serverAddress,
                                             @NotNull Integer port) throws IOException {
        Preconditions.checkNotNull(serverAddress, "serverAddress must not be null.");
        Preconditions.checkNotNull(port, "port must not be null.");

        return to(new Socket(serverAddress, port));
    }

    /**
     * Receives {@link ChatMessage} from socket.
     */
    @NotNull
    public ChatMessage receive() throws IOException, ClassNotFoundException {
        return JSON.valueOf((String) inputStream.readObject()).to(ChatMessage.class);
    }

    /**
     * Sends {@link ChatMessage}.
     */
    public void send(@NotNull ChatMessage chatMessage) throws IOException {
        outputStream.writeObject(JSON.format(chatMessage));
    }

    /**
     * Wraps string to default {@link ChatMessage} and sends it.
     */
    public void send(@NotNull String message) throws IOException {
        send(new ChatMessage(message));
    }

    public boolean isActive() {
        return socket.isConnected();
    }

    /**
     * Closes the connection.
     */
    public void kill() {
        try {
            if (outputStream != null)
                outputStream.close();
            if (inputStream != null)
                inputStream.close();
            if (socket != null)
                socket.close();
        } catch (Exception ignored) {
            // IGNORED
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public Integer getPort() {
        return port;
    }
}
