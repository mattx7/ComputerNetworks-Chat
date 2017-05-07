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
    @NotNull
    private ClientEntity client;

    /**
     * Constructor.
     *
     * @param client not null.
     */
    ServerListener(@NotNull final ClientEntity client) {
        Preconditions.checkNotNull(client, "client must not be null.");

        this.client = client;
    }

    /**
     * @see Thread#run()
     */
    public void run() {
        while (client.connection.isActive()) {
            try {
                client.display(client.connection.receive());
            } catch (final Exception e) {
                client.display("Server has closed the connection!");
                client.connection.kill();
            }
        }
    }
}
