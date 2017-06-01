package chat_app.client;

import org.apache.log4j.Logger;

import java.util.Scanner;

/**
 * <p>
 * To connect the Client in console mode use one of the following command <br />
 * > java ClientEntity [username] [port] [serverAddress]
 * </p><p>
 * <b>Defaults:</b>
 * - portNumber is 1500 <br />
 * - address is "localhost" <br />
 * - username is "Anonymous"
 * </p><p>
 * > java Client <br />
 * is equivalent to <br />
 * > java Client Anonymous 1500 localhost <br />
 * </p>
 */
public class ClientMain {
    private static final Logger LOG = Logger.getLogger(ClientMain.class);

    /**
     * @see ClientMain
     */
    public static void main(String[] args) {
        int portNumber = 1500;
        String address = "localhost";
        String userName = "Anonymous";

        switch (args.length) {
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                } catch (final NumberFormatException e) {
                    LOG.info("Invalid port number.");
                    usage();
                    return;
                }
            case 1:
                address = args[0];;
            case 0:
                break;
            default:
                usage();
                return;
        }
        ClientEntity client = new ClientEntity(userName);

        // Connect to server
        try {
            client.connect(address, portNumber);
        } catch (ServerNotFoundException e) {
            LOG.info("Sorry, can't find server!");
            return;
        }

        // wait for messages from user
        Scanner scan = new Scanner(System.in);
        while (client.connection.isActive()) {
            if (scan.hasNext()) {
                System.out.print("> ");
                String msg = scan.nextLine();

                client.sendMessage(msg);

            }
        }
    }

    public static void usage() {
        System.out.println("ClientEntity usage: > java Client [address] [port]");
    }

}