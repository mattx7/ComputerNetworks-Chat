package chat_app.server;

import chat_app.utility.Connection;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat-Server holds the {@link ChatRoom chat-rooms} and handles the first connection. <br/>
 * All new clients will be allocate to a default chat-room "Waiting-Hall".
 */
class ServerEntity {
    private static final Logger LOG = Logger.getLogger(ServerEntity.class);

    /**
     * Unique ID for each connection
     */
    private static int clientIdSequence;

    /**
     * Holds the server socket.
     */
    private ServerSocket serverSocket;

    /**
     * Holds the chat rooms.
     */
    @NotNull
    private List<ChatRoom> chatRooms;

    /**
     * Holds the waiting hall.
     */
    private ChatRoom waitingHall;

    /**
     * Will be turned of to stop the server.
     */
    private boolean keepGoing;

    /**
     * ServerEntity port.
     */
    @NotNull
    private Integer port;


    /**
     * Constructor.
     *
     * @param port server port. Not null.
     */
    ServerEntity(@NotNull final Integer port) {
        Preconditions.checkNotNull(port, "port must not be null.");

        this.port = port;
        chatRooms = new ArrayList<>();
        waitingHall = new ChatRoom(this, "Waiting-Hall");
        chatRooms.add(waitingHall);
        LOG.debug("ServerEntity created.");
    }

    /**
     * Starts the server
     */
    void start() {
        keepGoing = true;

        // create socket server and wait for connection requests
        try {
            // The socket used by the server
            serverSocket = new ServerSocket(port);

            // Infinite loop to wait for connections
            while (keepGoing) {
                Socket socket = serverSocket.accept();    // accept connection
                waitingHall.enterChatRoom(Connection.to(socket));
                if (!keepGoing)
                    break;
            }

            // Close all connections
            closeAllConnections();

        } catch (final IOException e) { // something went wrong
            LOG.error("Exception on new ServerSocket", e);
        }

        LOG.debug("ServerEntity started.");
    }

    /**
     * Creates and adds a new chat room.
     *
     * @param name Name of the room. Not null.
     */
    synchronized void addRoom(@NotNull final String name) {
        Preconditions.checkNotNull(name, "name must not be null.");

        this.chatRooms.add(new ChatRoom(this, name));
    }

    /**
     * Returns room by name or null if not found.
     *
     * @param name Name of the Room. Not null.
     * @return Not null.
     * @throws ChatRoomNotFoundException If no room with given name exists.
     */
    @NotNull
    synchronized ChatRoom getRoomByName(@NotNull final String name) throws ChatRoomNotFoundException {
        Preconditions.checkNotNull(name, "name must not be null.");

        for (final ChatRoom room : chatRooms) {
            if (room.getName().equalsIgnoreCase(name)) {
                return room;
            }
        }
        throw new ChatRoomNotFoundException();
    }

    /**
     * @return new client id. Not null.
     */
    @NotNull
    Integer getClientIdFromSequence() {
        return ++clientIdSequence;
    }

    /**
     * @return List of all chat-rooms. Not null.
     */
    @NotNull
    List<ChatRoom> getAllChatRooms() {
        return chatRooms;
    }

    /**
     * Closes all client-threads and IOStreams.
     */
    private void closeAllConnections() {
        LOG.debug("Close all connections...");
        try {
            serverSocket.close();
            for (final ChatRoom chatRoom : chatRooms) {
                for (final ConnectedClient client : chatRoom.getClients()) {
                    client.disconnect();
                }
            }

        } catch (final Exception ignored) {
        }
        LOG.debug("All connections closed!");
    }

    public ChatRoom getWaitingHall() {
        return waitingHall;
    }
}


