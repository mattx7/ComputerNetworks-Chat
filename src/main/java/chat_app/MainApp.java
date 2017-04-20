package chat_app;

import chat_app.server.Main;

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
                Main.main(newArgs);
            } else if (Objects.equals(args[0], "Client")) {
                chat_app.client.Main.main(newArgs);
            } else {
                usage();
            }
        } else {
            usage();
        }

    }

    private static void usage() {
        Main.usage();
        chat_app.client.Main.usage();
    }

}
