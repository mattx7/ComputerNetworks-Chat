package chat_app.client;

import chat_app.message.ChatMessage;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Chat ClientEntity
 */
class ClientEntity {
    private static final Logger LOG = Logger.getLogger(ClientEntity.class);

    ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;

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
     * @param serverAddress not null.
     * @param username      not null.
     * @param port          not null.
     */
    ClientEntity(@NotNull String serverAddress, @NotNull String username, int port) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.port = port;
    }

    /**
     * Starts the connection to the server
     *
     * @return true if successful
     */
    boolean start() { // TODO boolean method ersetzen

        try {
            socket = new Socket(serverAddress, port);
        } catch (final IOException e) {
            LOG.error("Error:", e);
            return false;
        }

        display("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            LOG.error("Error:", e);
            return false;
        }

        new ServerListener(this).start();

        // Send our username to the serverAddress this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try {
            outputStream.writeObject(username);
        } catch (IOException eIO) {
            disconnect();
            return false;
        }
        // success we inform the caller that it worked
        return true;
    }

    /**
     * To send message to serverAddress.
     */
    void sendMessage(@NotNull ChatMessage msg) {
        try {
            outputStream.writeObject(msg);
            LOG.debug(username + " has send a message");
        } catch (IOException e) {
            LOG.error("Error:", e);
        }
    }

    /**
     * Close connection and streams.
     */
    void disconnect() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (Exception e) {
            LOG.error("Error:", e);
        }
    }

    /**
     * To display in terminal
     */
    void display(@NotNull String msg) {
        System.out.println(msg);
    }
}
