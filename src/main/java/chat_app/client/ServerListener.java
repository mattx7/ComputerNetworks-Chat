package chat_app.client;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Server listener thread.
 */
class ServerListener extends Thread {
    private final Logger LOG = Logger.getLogger(ServerListener.class);

    private ClientEntity client;

    public ServerListener(@NotNull ClientEntity client) {
        Preconditions.checkNotNull(client, "client must not be null.");

        this.client = client;
    }

    public void run() {
        while (true) {
            try {
                String msg = (String) client.inputStream.readObject();
                client.display(msg);
            } catch (final Exception e) {
                LOG.error("Server has closed the connection!", e);
                break;
            }
        }
    }
}
