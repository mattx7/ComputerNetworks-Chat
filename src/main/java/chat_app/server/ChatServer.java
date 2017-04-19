package chat_app.server;

import chat_app.message.ChatMessage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Chat server
 */
public class ChatServer {
    private static final Logger LOG = Logger.getLogger(ChatServer.class);

    /**
     * Unique ID for each connection TODO ?
     */
    private static int uniqueId;

    /**
     * Keeps the list of the Clients.
     */
    private ArrayList<ClientThread> clientThreads;

    /**
     * Will be turned of to stop the server
     */
    private boolean keepGoing;

    private SimpleDateFormat dateFormatter;
    private int port;

    ChatServer(int port) {
        this.port = port;
        dateFormatter = new SimpleDateFormat("HH:mm:ss");
        clientThreads = new ArrayList<>();
    }

    /**
     * Starts the server
     */
    void start() {
        keepGoing = true;
        /* create socket server and wait for connection requests */
        try {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while (keepGoing) {
                // format message saying we are waiting

                Socket socket = serverSocket.accept();    // accept connection
                // if I was asked to stop
                if (!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);  // make a thread of it
                clientThreads.add(t);                                    // save it in the ArrayList
                t.start();
            }
            // I was asked to stop
            try {
                serverSocket.close();
                for (ClientThread tc : clientThreads) {
                    try {
                        tc.inputStream.close();
                        tc.outputStream.close();
                        tc.socket.close();
                    } catch (final IOException ioE) {
                        // not much I can do
                    }
                }
            } catch (final Exception ignored) {

            }
        }
        // something went bad
        catch (final IOException e) {
            String msg = dateFormatter.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
        }
    }

    /**
     * Stops the server
     */
    protected void stop() {
        keepGoing = false;
        // connect to myself as Client to exit statement
        // Socket socket = serverSocket.accept();
        try {
            new Socket("localhost", port);
        } catch (Exception e) {
            // nothing I can really do
        }
    }

    /**
     * To broadcast a message to all Clients
     */
    private synchronized void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = dateFormatter.format(new Date());
        String messageLf = time + " " + message + "\n";

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for (int i = clientThreads.size(); --i >= 0; ) {
            ClientThread ct = clientThreads.get(i);
            // try to write to the Client if it fails remove it from the list
            if (!ct.writeMsg(messageLf)) {
                clientThreads.remove(i);

            }
        }
    }

    /**
     * For a client who logoff using the LOGOUT message
     *
     * @param id From Client.
     */
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for (int i = 0; i < clientThreads.size(); ++i) {
            ClientThread client = clientThreads.get(i);
            // found it
            if (client.id == id) {
                clientThreads.remove(i);
                return;
            }
        }
    }

    /**
     * One instance of this thread will run for each client.
     */
    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;

        /**
         * Unique if (easier for deconnection)
         */
        int id;

        /**
         * Username of the client.
         */
        String username;

        /**
         * MESSAGE
         */
        ChatMessage message;

        /**
         * Date of connection.
         */
        String date;

        /**
         * Constructor.
         *
         * @param socket not null.
         */
        ClientThread(Socket socket) {
            id = ++uniqueId;
            this.socket = socket;

			// Creating Data Streams
            LOG.debug("Thread trying to create Object Input/Output Streams");
            try {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());

                username = (String) inputStream.readObject();

            } catch (final IOException e) {
                LOG.error("Thread couldn't create streams", e);
                return;
            } catch (final ClassNotFoundException ignored) {
            }
            date = new Date().toString() + "\n";
        }


        public void run() {
            boolean keepGoing = true;

            while (keepGoing) {
                // get message
                try {
                    message = (ChatMessage) inputStream.readObject();
                } catch (final IOException e) {
                    LOG.error("Thread couldn't read object", e);
                    break;
                } catch (final ClassNotFoundException e) {
                    LOG.error("Thread couldn't read object", e);
                    break;
                }

                // read message TODO comments to methods
                String message = this.message.getMessage();

                // Type of message receive
                switch (this.message.getType()) {
                    case MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case LOGOUT:
                        keepGoing = false;
                        break;
                    case WHO_IS_IN:
                        writeMsg("List of the users connected at " + dateFormatter.format(new Date()) + "\n");
                        // scan clientThreads the users connected
                        for (int i = 0; i < clientThreads.size(); ++i) {
                            ClientThread ct = clientThreads.get(i);
                            writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }

            // remove myself from the arrayList containing the list of the connected Clients
            remove(id);
            close();
        }

        private void close() {
            try {
                if (outputStream != null) outputStream.close();
            } catch (Exception ignored) {
            }
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception ignored) {
            }
            try {
                if (socket != null) socket.close();
            } catch (Exception ignored) {
            }
        }

        /**
         * Write a String to the Client output stream
         */
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }

            // write the message to the stream
            try {
                outputStream.writeObject(msg);
            } catch (final IOException e) {
                LOG.debug("Couldn't write message to stream", e);
                // if an error occurs, do not abort just inform the user
            }
            return true;
        }
    }
}


