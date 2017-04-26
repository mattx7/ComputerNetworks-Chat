package chat_app.server;

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
    ServerEntity(@NotNull Integer port) {
        Preconditions.checkNotNull(port, "port must not be null.");

        this.port = port;
        chatRooms = new ArrayList<>();
        waitingHall = new ChatRoom(this, "Waiting-Hall");
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
                waitingHall.enterChatRoom(socket);
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
     * Stops the server TODO STOP() einbinden
     */
    void stop() {
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
     * Creates and adds a new chat room.
     *
     * @param name Name of the room. Not null.
     */
    void addRoom(@NotNull String name) {
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
    ChatRoom getRoomByName(@NotNull String name) throws ChatRoomNotFoundException {
        Preconditions.checkNotNull(name, "name must not be null.");

        for (ChatRoom room : chatRooms) {
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
            for (ChatRoom chatRoom : chatRooms) {
                for (ConnectedClient client : chatRoom.clients) {
                    try {
                        client.inputStream.close();
                        client.outputStream.close();
                        client.socket.close();
                    } catch (final IOException e) {
                        LOG.error("Exception on close Clients Threads", e);
                    }
                }
            }

        } catch (final Exception ignored) {

        }
        LOG.debug("All connections closed!");
    }
}


