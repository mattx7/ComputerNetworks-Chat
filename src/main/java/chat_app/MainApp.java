package chat_app;

import chat_app.client.ClientApp;
import chat_app.server.ServerApp;

import java.util.Arrays;
import java.util.Objects;

/**
 * Main application for server and client.
 */
public class MainApp {

    /**
     * Can start server and client.
     */
    public static void main(String[] args) {

        if (args.length >= 1) {
            final String[] newArgs = Arrays.copyOfRange(args, 1, args.length); // delete first element

            if (Objects.equals(args[0], "Server")) {
                ServerApp.main(newArgs);
            } else if (Objects.equals(args[0], "Client")) {
                ClientApp.main(newArgs);
            }
        } else {
            usage();
        }

    }

    private static void usage() {
        ServerApp.usage();
        ClientApp.usage();
    }

}
