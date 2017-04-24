package chat_app.server;

import chat_app.message.ChatMessage;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * One instance of this thread will run for each client.
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
     * Holds the reference to the room where the client is member of. TODO better english
     */
    private ChatRoom chatRoom;

    /**
     * Holds dateOfConnection formatter for messages.
     */
    private SimpleDateFormat dateFormatter;

    /**
     * Holds the reference to the server instance.
     */
    ServerEntity server;

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
        this.clientId = chatRoom.getClientIdFromSequence();
        this.socket = socket;
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
                    deliverMessage("List of the users connected at " + dateFormatter.format(new Date()) + "\n");
                    // scan clientThreads the users connected
                    for (int i = 0; i < chatRoom.clientThreads.size(); ++i) { // TODO make client list private?
                        ConnectedClient clientThread = chatRoom.clientThreads.get(i);
                        deliverMessage((i + 1) + ") " + clientThread.username + " since " + clientThread.dateOfConnection);
                    }
                    break;
                case CREATE_ROOM:
                    final String nameOfNewRoom = chatMessage.getMessage();
                    createChatRoom(nameOfNewRoom); // addRoom in room macht eigentlich kein sinn aber sollte
                    deliverMessage("Created Room " + nameOfNewRoom);
                    LOG.debug("Created Room " + nameOfNewRoom);
                    break;
                case SWITCH_ROOM:
                    final String nameOfRoom = chatMessage.getMessage();
                    try {
                        // first enter then leave to avoid a state without chat room.
                        leaveChatRoom();
                        enterChatRoom(nameOfRoom);
                        LOG.debug(username + " switched to room " + nameOfRoom);
                    } catch (ChatRoomNotFoundException e) {
                        LOG.error(username + " could't enter" + nameOfRoom, e);
                    }
                    break;
            }
        }

        leaveChatRoom();
        close();
    }

    /**
     * Distributes a message to all clients.
     *
     * @param chatMessage not null.
     */
    private void distributeMessage(@NotNull ChatMessage chatMessage) {
        Preconditions.checkNotNull(chatMessage, "chatMessage must not be null.");

        chatRoom.distributeMessage(username + ": " + chatMessage.getMessage());
    }

    /**
     * Enters new chat-room.
     */
    private void enterChatRoom(@NotNull String nameOfRoom) throws ChatRoomNotFoundException {
        Preconditions.checkNotNull(nameOfRoom, "nameOfRoom must not be null.");

        final ChatRoom room = getRoomByName(nameOfRoom);
        if (room != null) {
            room.enterChatRoom(this);
            this.chatRoom = room;
        } else {
            throw new ChatRoomNotFoundException();
        }
    }

    /**
     * Leave the actual room.
     */
    private void leaveChatRoom() {
        chatRoom.removeClientFromRoom(this.clientId);
        // chatRoom = null; will be overwritten later
    }

    /**
     * Returns room or null if not found.
     */
    @Nullable
    private ChatRoom getRoomByName(@NotNull String nameOfRoom) {
        Preconditions.checkNotNull(nameOfRoom, "nameOfRoom must not be null.");

        return server.getRoomByName(nameOfRoom);
    }

    /**
     * Creates a new chat room.
     */
    private void createChatRoom(@NotNull String nameOfNewRoom) {
        Preconditions.checkNotNull(nameOfNewRoom, "nameOfNewRoom must not be null.");

        chatRoom.addRoom(nameOfNewRoom);
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
     * @return username.
     */
    @NotNull
    String getUsername() {
        return username;
    }

    /**
     * Write a String to the Client output stream.
     */
    boolean deliverMessage(@NotNull String message) {
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
}