package chat_app.server;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Chat-Room holds connected clients and sends text messages to them.
 */
class ChatRoom {
    private static final Logger LOG = Logger.getLogger(ChatRoom.class);

    /**
     * Holds the list of the Clients.
     */
    private List<ConnectedClient> clients;

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
    private ServerEntity server;

    /**
     * Constructor.
     *
     * @param server Reference to the sever instance. Not null.
     * @param name Name of the chat room. Not null.
     */
    ChatRoom(@NotNull ServerEntity server, @NotNull String name) {
        Preconditions.checkNotNull(server, "server must not be null.");
        Preconditions.checkNotNull(name, "name must not be null.");

        clients = new ArrayList<>();
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
     * @param connectedClient Connection to the client. Not null.
     */
    synchronized void enterChatRoom(@NotNull ConnectedClient connectedClient) {
        Preconditions.checkNotNull(connectedClient, "connectedClient must not be null.");

        LOG.debug("Client enters the room " + name);
        clients.add(connectedClient);
        connectedClient.deliverMessage("Welcome in Room " + name);
        distributeMessage(connectedClient.getUsername() + " has entered.");
    }

    /**
     * Creates a new thread for each connection.
     *
     * @param socket Connection to the client. Not null.
     */
    synchronized void enterChatRoom(@NotNull Socket socket) {
        Preconditions.checkNotNull(socket, "socket must not be null.");

        final ConnectedClient connectedClient = new ConnectedClient(server, this, socket);
        connectedClient.start();
        enterChatRoom(connectedClient);
    }

    /**
     * To distribute a message to all Clients
     *
     * @param message Not null.
     */
    synchronized void distributeMessage(@NotNull String message) {
        Preconditions.checkNotNull(message, "message must not be null.");

        // add HH:mm:ss and \n to the message
        String time = dateFormatter.format(new Date());
        String messageFormatted = time + " " + message;
        LOG.debug("Room[" + name + "] <<< " + messageFormatted);


        // we loop in reverse order in case we would have to removeClientFromRoom a Client
        // because it has disconnected
        for (int i = clients.size(); --i >= 0; ) {
            ConnectedClient clientThread = clients.get(i);
            // try to write to the Client if it fails removeClientFromRoom it from the list
            if (!clientThread.deliverMessage(messageFormatted + "\n")) {
                clients.remove(i);

            }
        }
    }

    /**
     * For a client who logoff using the LOGOUT message
     *
     * @param id From Client. Not null.
     */
    synchronized void removeClientFromRoom(@NotNull Integer id) {
        Preconditions.checkNotNull(id, "id must not be null.");

        // scan the array list until we found the Id
        for (int i = 0; i < clients.size(); ++i) {
            ConnectedClient client = clients.get(i);

            if (client.clientId == id) {
                clients.remove(i);
                return;
            }
        }
    }

    /**
     * @return Unmodifiable List of connected clients.
     */
    synchronized List<ConnectedClient> getClients() {
        return Collections.unmodifiableList(this.clients);
    }

}
