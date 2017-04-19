package chat_app.client;

import chat_app.message.ChatMessage;
import chat_app.message.ChatMessageType;
import org.apache.log4j.Logger;

import java.util.Scanner;

/**
 * <p>
 * To start the Client in console mode use one of the following command <br />
 * > java Client [username] [port] [serverAddress]
 * </p><p>
 * <b>Defaults:</b>
 * - portNumber is 1500 <br />
 * - address is "localhost" <br />
 * - username is "Anonymous"
 * </p><p>
 * > java Client <br />
 * is equivalent to <br />
 * > java Client Anonymous 1500 localhost <br />
 * </p><p>
 * In console mode, if an error occurs the program simply stops
 * </p>
 */
public class ClientApp {
    private static final Logger LOG = Logger.getLogger(ClientApp.class);

    /**
     * @see ClientApp
     */
    public static void main(String[] args) {
        int portNumber = 1500;
        String address = "localhost";
        String userName = "Anonymous";

        switch (args.length) {
            case 3:
                address = args[2];
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    usage();
                    return;
                }
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                usage();
                return;
        }
        ChatClient client = new ChatClient(address, userName, portNumber);

        // check for server
        if (!client.start()) {
            LOG.error("Can't connect to server!");
            return;
        }

        // wait for messages from user
        Scanner scan = new Scanner(System.in);
        while (true) { // TODO replace while(true)
            System.out.print("> ");
            String msg = scan.nextLine();

            // LOGOUT
            if (msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(new ChatMessage(ChatMessageType.LOGOUT, ""));
                break;
            }

            // WHO_IS_IN
            else if (msg.equalsIgnoreCase("WHOISIN")) {
                client.sendMessage(new ChatMessage(ChatMessageType.WHO_IS_IN, ""));
            } else {
                // DEFAULT
                client.sendMessage(new ChatMessage(ChatMessageType.MESSAGE, msg));
            }
        }

        client.disconnect();
    }

    public static void usage() {
        System.out.println("Client usage: > java Client [username] [port] [address]");
    }

}