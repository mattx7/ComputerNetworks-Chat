package chat_app.server;

import chat_app.message.ChatMessage;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * One instance of this thread will run for each client. Receives {@link chat_app.message.ChatMessage} from the client.
 */
class ConnectedClient extends Thread {
    private static final Logger LOG = Logger.getLogger(ConnectedClient.class);

    /**
     * Unique if (easier for deconnection)
     */
    int clientId;

    /**
     * Socket to the client.
     */
    Socket socket;

    /**
     * Input-Stream from client.
     */
    ObjectInputStream inputStream;

    /**
     * Output-Stream to client.
     */
    ObjectOutputStream outputStream;

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

    /**
     * Constructor.
     *
     * @param socket not null.
     */
    ConnectedClient(@NotNull ServerEntity server, @NotNull ChatRoom chatRoom, @NotNull Socket socket) {
        Preconditions.checkNotNull(server, "server must not be null.");
        Preconditions.checkNotNull(chatRoom, "chatRoom must not be null.");
        Preconditions.checkNotNull(socket, "socket must not be null.");

        this.server = server;
        this.chatRoom = chatRoom;
        this.socket = socket;
        this.clientId = server.getClientIdFromSequence();
        this.dateOfConnection = new Date().toString() + "\n";
        this.dateFormatter = new SimpleDateFormat("HH:mm:ss");

        // Creating Data Streams
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            username = (String) inputStream.readObject();

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
        ChatMessage chatMessage;

        while (keepGoing) {
            // get chatMessage
            try {
                chatMessage = (ChatMessage) inputStream.readObject();
                LOG.debug("ServerEntity receives message...");
            } catch (final Exception e) {
                LOG.error("Thread couldn't read object", e);
                break;
            }

            // Type of chatMessage receive
            switch (chatMessage.getType()) {
                case MESSAGE:
                    distributeMessage(chatMessage);
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
                    final String nameOfNewRoom = chatMessage.getMessage();
                    createChatRoom(nameOfNewRoom);
                    deliverMessage("Created Room " + nameOfNewRoom);
                    LOG.debug("Created Room " + nameOfNewRoom);
                    break;
                case SWITCH_ROOM:
                    final String nameOfRoom = chatMessage.getMessage();
                    try {
                        // first enter then leave to avoid a state without chat room.
                        ChatRoom room = getRoomByName(nameOfRoom);
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
        close();
    }

    /**
     * @return username.
     */
    @NotNull
    String getUsername() {
        return username;
    }

    /**
     * Write a String to the Client output stream.
     */
    boolean deliverMessage(@NotNull String message) { // TODO Bool -> Exc
        Preconditions.checkNotNull(message, "message must not be null.");

        // if Client is still connected send the message to it
        if (!socket.isConnected()) {
            close();
            return false;
        }

        // write the message to the stream
        try {
            outputStream.writeObject(message);
        } catch (final IOException e) {
            LOG.debug("Couldn't write message to stream", e);
            // if an error occurs, do not abort just inform the user
        }
        return true;
    }

    /**
     * Delivers available rooms to client.
     */
    private void deliverAvailableRooms() {
        final List<ChatRoom> chatRooms = server.getAllChatRooms();

        if (chatRooms.size() != 0) {
            // Print rooms
            deliverMessage("List of all chat-rooms: \n");
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
        // Print clients
        for (int i = 0; i < chatRoom.clients.size(); ++i) {
            ConnectedClient client = chatRoom.clients.get(i);
            deliverMessage((i + 1) + ".) " + client.username + " since " + client.dateOfConnection);
        }
    }

    /**
     * Distributes a message to all clients.
     *
     * @param chatMessage not null.
     */
    private void distributeMessage(@NotNull ChatMessage chatMessage) {
         chatRoom.distributeMessage(username + ": " + chatMessage.getMessage());
    }

    /**
     * Enters new chat-room.
     */
    private void enterChatRoom(@NotNull ChatRoom room) {
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
    private ChatRoom getRoomByName(@NotNull String nameOfRoom) throws ChatRoomNotFoundException {
          return server.getRoomByName(nameOfRoom);
    }

    /**
     * Creates a new chat room.
     */
    private void createChatRoom(@NotNull String nameOfNewRoom) {
        server.addRoom(nameOfNewRoom);
    }

    /**
     * Closes IOStreams and socket.
     */
    private void close() {
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {
            // IGNORED
        }
    }
}