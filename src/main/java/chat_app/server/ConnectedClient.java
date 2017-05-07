package chat_app.server;

import chat_app.transfer_object.Message;
import chat_app.utility.Connection;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * One instance of this thread will run for each client. Receives {@link Message} from the client.
 */
class ConnectedClient extends Thread {
    private static final Logger LOG = Logger.getLogger(ConnectedClient.class);

    /**
     * Unique if (easier for disconnection)
     */
    int clientId;

    /**
     * Connection to the client.
     */
    private Connection connection;

    /**
     * Username of the client.
     */
    private String username;

    /**
     * Date of connection.
     */
    private String dateOfConnection;

    /**
     * Holds the reference to the room where the client is member of.
     */
    private ChatRoom chatRoom;

    /**
     * Holds dateOfConnection formatter for messages.
     */
    private SimpleDateFormat dateFormatter;

    /**
     * Holds the reference to the server instance.
     */
    private ServerEntity server;

    ConnectedClient(@NotNull ServerEntity server,
                    @NotNull ChatRoom chatRoom,
                    @NotNull Connection connection) {
        Preconditions.checkNotNull(server, "server must not be null.");
        Preconditions.checkNotNull(chatRoom, "chatRoom must not be null.");
        Preconditions.checkNotNull(connection, "socket must not be null.");

        this.server = server;
        this.chatRoom = chatRoom;
        this.clientId = server.getClientIdFromSequence();
        this.dateOfConnection = new Date().toString() + "\n";
        this.dateFormatter = new SimpleDateFormat("HH:mm:ss");
        this.connection = connection;

        // Creating Data Streams
        try {
            username = connection.receive().getPayload();
            LOG.debug("Thread has created IOStreams for new client");

        } catch (final Exception e) {
            LOG.error("Thread couldn't create IOStreams", e);
        }
    }


    /**
     * @see Thread#run()
     */
    public void run() {
        boolean keepGoing = true;
        Message message;

        while (keepGoing) {
            // get message
            try {
                message = connection.receive();
                LOG.debug("ServerEntity receives transfer_object...");
            } catch (final Exception e) {
                LOG.error("Thread couldn't read object", e);
                break;
            }

            // Type of message receive
            switch (message.getType()) {
                case MESSAGE:
                    distributeMessage(message);
                    break;
                case HELP:
                    deliverHelp();
                    break;
                case LOGOUT:
                    keepGoing = false;
                    break;
                case WHO_IS_IN:
                    deliverWhoIsIn();
                    break;
                case AVAILABLE_ROOMS:
                    deliverAvailableRooms();
                    break;
                case CREATE_ROOM:
                    final String nameOfNewRoom = message.getPayload();
                    createChatRoom(nameOfNewRoom);
                    deliverMessage("Created Room " + nameOfNewRoom);
                    LOG.debug("Created Room " + nameOfNewRoom);
                    break;
                case SWITCH_ROOM:
                    final String nameOfRoom = message.getPayload();
                    try {
                        // first enter then leave to avoid a state without chat room.
                        ChatRoom room;
                        if (nameOfRoom.equals("")) {
                            room = getRoomByName(nameOfRoom);
                        } else {
                            room = getWaitingHall();
                        }
                        leaveChatRoom();
                        enterChatRoom(room);
                        LOG.debug(username + " switched to room " + nameOfRoom);
                    } catch (ChatRoomNotFoundException e) {
                        LOG.error(username + " could't enter " + nameOfRoom);
                        deliverMessage("Sorry, couldn't find room " + nameOfRoom);
                        deliverAvailableRooms();
                    }
                    break;
            }
        }

        leaveChatRoom();
        connection.kill();
    }

    /**
     * @return username.
     */
    @NotNull
    String getUsername() {
        return username;
    }

    /**
     * Kills the connection.
     */
    void disconnect() {
        connection.kill();
    }

    /**
     * Write a String to the Client output stream.
     */
    boolean deliverMessage(@NotNull final String message) {
        Preconditions.checkNotNull(message, "transfer_object must not be null.");

        // PRECONDITION: Client still connected.
        if (connection.isInactive()) {
            connection.kill();
            return false;
        }

        try {
            connection.send(message);
            return true;
        } catch (final IOException e) {
            // if an error occurs, do not abort just inform the user
            LOG.debug("Couldn't write transfer_object to stream", e);
            return false;
        }
    }

    /**
     * Delivers help message.
     */
    private void deliverHelp() {
        deliverMessage("" +
                "1.) LOGOUT for Logout, \n" +
                "2.) WHOISIN to see logged in clients, \n" +
                "3.) AVAILABLE to get all available rooms \n" +
                "4.) CREATE to create a new room \n" +
                "5.) SWITCH to switch ro another room \n");
    }

    /**
     * Delivers available rooms to client.
     */
    private void deliverAvailableRooms() {
        final List<ChatRoom> chatRooms = server.getAllChatRooms();

        if (chatRooms.size() != 0) {
            // Print rooms
            deliverMessage("List of all chat-rooms:");
            for (int i = 0; i < chatRooms.size(); ++i) {
                ChatRoom chatRoom = chatRooms.get(i);
                deliverMessage((i + 1) + ".) " + chatRoom.getName());
            }
        } else {
            deliverMessage("Currently are no chat-rooms available. You can create one with CREATE <NAME> \n");
        }
    }

    /**
     * Delivers connected clients to client.
     */
    private void deliverWhoIsIn() {
        deliverMessage("List of the users connected at " + dateFormatter.format(new Date()) + "\n");
        List<ConnectedClient> clients = chatRoom.getClients();
        for (int i = 0; i < clients.size(); ++i) {
            ConnectedClient client = clients.get(i);
            deliverMessage((i + 1) + ".) " + client.username + " since " + client.dateOfConnection);
        }
    }

    /**
     * Distributes a transfer_object to all clients.
     *
     * @param message not null.
     */
    private void distributeMessage(@NotNull final Message message) {
        chatRoom.distributeMessage(username + ": " + message.getPayload());
    }

    /**
     * Enters new chat-room.
     */
    private void enterChatRoom(@NotNull final ChatRoom room) {
        room.enterChatRoom(this);
        this.chatRoom = room;
    }

    /**
     * Leave the actual room.
     */
    private void leaveChatRoom() {
        LOG.debug(username + " is leaving...");
        chatRoom.removeClientFromRoom(this.clientId);
        chatRoom = null;
    }

    /**
     * Returns room or null if not found.
     *
     * @throws ChatRoomNotFoundException If no room with given name exists.
     */
    @NotNull
    private ChatRoom getRoomByName(@NotNull final String nameOfRoom) throws ChatRoomNotFoundException {
        return server.getRoomByName(nameOfRoom);
    }

    /**
     * Creates a new chat room.
     */
    private void createChatRoom(@NotNull final String nameOfNewRoom) {
        server.addRoom(nameOfNewRoom);
    }

    /**
     * @return Waiting-Hall from server.
     */
    private ChatRoom getWaitingHall() {
        return server.getWaitingHall();
    }
}