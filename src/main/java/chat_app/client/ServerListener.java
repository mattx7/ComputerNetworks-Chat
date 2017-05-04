package chat_app.client;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * Listens to the server for incoming messages and displays it to the client.
 */
class ServerListener extends Thread {
    /**
     * Holds client reference.
     */
    private ClientEntity client;

    /**
     * Constructor.
     *
     * @param client not null.
     */
    ServerListener(@NotNull ClientEntity client) {
        Preconditions.checkNotNull(client, "client must not be null.");

        this.client = client;
    }

    /**
     * @see Thread#run()
     */
    public void run() {
        while (true) { // TODO replace while true
            try {
                client.display(client.connection.receive());
            } catch (final Exception e) {
                client.display("Server has closed the connection!");
                break;
            }
        }
    }
}
