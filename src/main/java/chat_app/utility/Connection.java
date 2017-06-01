package chat_app.utility;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Holds all useful stuff for connection.
 */
public class Connection {

    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    private String serverAddress;
    private Integer port;

    private Connection(Socket socket) throws IOException {
        this.socket = socket;
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));

        this.serverAddress = socket.getInetAddress().toString();
        this.port = socket.getPort();
    }

    /**
     * Creates new instance.
     */
    public synchronized static Connection to(@NotNull final String serverAddress,
                                             @NotNull final Integer port) throws IOException {
        Preconditions.checkNotNull(serverAddress, "serverAddress must not be null.");
        Preconditions.checkNotNull(port, "port must not be null.");

        return new Connection(new Socket(serverAddress, port));
    }

    /**
     * Receives {@link String} from socket.
     */
    @NotNull
    public String receive() throws IOException, ClassNotFoundException {
        return inputStream.readUTF();
    }

    /**
     * Sends {@link String}.
     */
    public void send(@NotNull final String message) throws IOException {
        outputStream.writeUTF(message);
        outputStream.flush();
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
