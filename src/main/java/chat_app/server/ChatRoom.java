package chat_app.server;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Chat-ChatRoom for clients.
 */
class ChatRoom {
    private static final Logger LOG = Logger.getLogger(ChatRoom.class);

    /**
     * Holds the list of the Clients.
     */
    ArrayList<ConnectedClientThread> clientThreads;

    /**
     * Holds the name of the chat room.
     */
    private String name;

    /**
     * Holds date formatter for messages.
     */
    private SimpleDateFormat dateFormatter;

    /**
     * Holds the server instance.
     */
    private Server server;

    /**
     * Constructor.
     *
     * @param name Name of the chat room.
     */
    ChatRoom(@NotNull Server server, @NotNull String name) {
        Preconditions.checkNotNull(server, "server must not be null.");
        Preconditions.checkNotNull(name, "name must not be null.");

        clientThreads = new ArrayList<>();
        this.name = name;
        this.server = server;
        dateFormatter = new SimpleDateFormat("HH:mm:ss");
    }

    /**
     * @return name.
     */
    @NotNull
    String getName() {
        return name;
    }

    /**
     * Creates a new thread for each connection.
     *
     * @param connectedClient Connection to the client.
     */
    void enterChatRoom(@NotNull ConnectedClientThread connectedClient) {
        Preconditions.checkNotNull(connectedClient, "connectedClient must not be null.");

        LOG.debug("Client enters the room " + name);
        clientThreads.add(connectedClient);
        connectedClient.deliverMessage("Welcome in Room " + name);
        distributeMessage(connectedClient.getUsername() + " has entered.");
    }

    /**
     * Creates a new thread for each connection.
     *
     * @param socket Connection to the client.
     */
    void enterChatRoom(@NotNull Socket socket) {
        Preconditions.checkNotNull(socket, "socket must not be null.");

        final ConnectedClientThread connectedClient = new ConnectedClientThread(server, this, socket);
        connectedClient.start();
        enterChatRoom(connectedClient);
    }

    /**
     * To distribute a message to all Clients
     */
    synchronized void distributeMessage(@NotNull String message) {
        Preconditions.checkNotNull(message, "message must not be null.");

        // add HH:mm:ss and \n to the message
        String time = dateFormatter.format(new Date());
        String messageLf = time + " " + message + "\n";
        LOG.debug("Room[" + name + "] <<< " + messageLf);


        // we loop in reverse order in case we would have to removeClientFromRoom a Client TODO ??
        // because it has disconnected
        for (int i = clientThreads.size(); --i >= 0; ) {
            ConnectedClientThread clientThread = clientThreads.get(i);
            // try to write to the Client if it fails removeClientFromRoom it from the list
            if (!clientThread.deliverMessage(messageLf)) { // TODO vlt immer gleich raus löschen?
                clientThreads.remove(i);

            }
        }
    }

    /**
     * For a client who logoff using the LOGOUT message
     *
     * @param id From Client.
     */
    synchronized void removeClientFromRoom(int id) {
        // scan the array list until we found the Id
        for (int i = 0; i < clientThreads.size(); ++i) {
            ConnectedClientThread client = clientThreads.get(i);

            if (client.clientId == id) {
                clientThreads.remove(i);
                return;
            }
        }
    }

    /**
     * @return new client id.
     */
    int getClientIdFromSequence() {
        return server.getClientIdFromSequence();
    }

    /**
     * Creates and adds a new chat room.
     *
     * @param name Name of the room.
     */
    void addRoom(@NotNull String name) {
        Preconditions.checkNotNull(name, "name must not be null.");

        this.server.addRoom(name);
    }

}
