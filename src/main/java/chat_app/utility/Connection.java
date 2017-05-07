package chat_app.utility;

import chat_app.transfer_object.Message;
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
    public synchronized static Connection to(@NotNull final Socket socket) throws IOException {
        Preconditions.checkNotNull(socket, "socket must not be null.");

        return new Connection(socket);
    }

    /**
     * Creates new instance.
     */
    public synchronized static Connection to(@NotNull final String serverAddress,
                                             @NotNull final Integer port) throws IOException {
        Preconditions.checkNotNull(serverAddress, "serverAddress must not be null.");
        Preconditions.checkNotNull(port, "port must not be null.");

        return to(new Socket(serverAddress, port));
    }

    /**
     * Receives {@link Message} from socket.
     */
    @NotNull
    public Message receive() throws IOException, ClassNotFoundException {
        return JSON.valueOf((String) inputStream.readObject()).to(Message.class);
    }

    /**
     * Sends {@link Message}.
     */
    public void send(@NotNull final Message message) throws IOException {
        outputStream.writeObject(JSON.format(message));
    }

    /**
     * Wraps string to default {@link Message} and sends it.
     */
    public void send(@NotNull final String message) throws IOException {
        send(new Message(message));
    }

    /**
     * @return True, if still connected.
     */
    public boolean isActive() {
        return !socket.isClosed();
    }

    /**
     * @return True, if connection is closed.
     */
    public boolean isInactive() {
        return !isActive();
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
