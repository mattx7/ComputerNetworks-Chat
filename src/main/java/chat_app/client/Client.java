package chat_app.client;

import chat_app.message.Message;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Chat Client
 */
class Client {
    private static final Logger LOG = Logger.getLogger(Client.class);

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;

    private String server;
    private String username;
    private int port;

    Client(String server, String username, int port) {
        this.server = server;
        this.username = username;
        this.port = port;
    }

    boolean start() {

        try {
            socket = new Socket(server, port);
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

        new ServerListener().start();

        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be Message objects
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
     * To send message to server.
     */
    void sendMessage(Message msg) {
        try {
            outputStream.writeObject(msg);
        } catch (IOException e) {
            LOG.error("Error:", e);
        }
    }

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
        } // not much else I can do
    }

    /**
     * To display in terminal
     */
    private void display(String msg) {
        System.out.println(msg);
    }

    class ServerListener extends Thread {
        private final Logger LOG = Logger.getLogger(ServerListener.class);

        public void run() {
            while (true) {
                try {
                    String msg = (String) inputStream.readObject();
                    display(msg);
                } catch (final IOException e) {
                    LOG.error("Error:", e);
                    display("Server has closed the connection!");
                    break;
                } catch (final ClassNotFoundException e) {
                    LOG.error("Error:", e);
                    break;
                }
            }
        }
    }
}
