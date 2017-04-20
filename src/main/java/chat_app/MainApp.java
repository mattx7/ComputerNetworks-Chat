package chat_app;

import chat_app.client.ClientMain;
import chat_app.server.ServerMain;

import java.util.Arrays;
import java.util.Objects;

/**
 * ClientMain application for server and client.
 */
public class MainApp {

    /**
     * Starts server and client.
     */
    public static void main(String[] args) {

        if (args.length >= 1) {
            final String[] newArgs = Arrays.copyOfRange(args, 1, args.length); // delete first element

            if (Objects.equals(args[0], "Server")) {
                ServerMain.main(newArgs);
            } else if (Objects.equals(args[0], "Client")) {
                ClientMain.main(newArgs);
            } else {
                usage();
            }
        } else {
            usage();
        }

    }

    /**
     * Shows full usage from server and client.
     */
    private static void usage() {
        ServerMain.usage();
        ClientMain.usage();
    }

}
