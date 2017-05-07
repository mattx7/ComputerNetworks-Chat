package chat_app.client;

import chat_app.transfer_object.Message;
import chat_app.transfer_object.MessageType;
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
            case 3:
                address = args[2];
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                } catch (final NumberFormatException e) {
                    LOG.info("Invalid port number.");
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

                final String[] msgInWords = msg.split(" ");
                String command = "";
                String nameOfRoom = "";
                if (msgInWords.length == 2) {
                    command = (msgInWords[0]);
                    nameOfRoom = (msgInWords[1]);
                }

                if (msg.equalsIgnoreCase("LOGOUT")) {
                    client.sendMessage(new Message(MessageType.LOGOUT));
                    break;

                } else if (msg.equalsIgnoreCase("WHOISIN")) {
                    client.sendMessage(new Message(MessageType.WHO_IS_IN));

                } else if (command.equalsIgnoreCase("SWITCH")) {
                    client.sendMessage(new Message(MessageType.SWITCH_ROOM, nameOfRoom));

                } else if (command.equalsIgnoreCase("CREATE")) {
                    client.sendMessage(new Message(MessageType.CREATE_ROOM, nameOfRoom));

                } else if (msg.equalsIgnoreCase("HELP")) {
                    client.sendMessage(new Message(MessageType.HELP));

                } else if (msg.equalsIgnoreCase("AVAILABLE")) {
                    client.sendMessage(new Message(MessageType.AVAILABLE_ROOMS));

                } else {
                    client.sendMessage(new Message(MessageType.MESSAGE, msg));
                }
            }
        }
    }

    public static void usage() {
        System.out.println("ClientEntity usage: > java Client [username] [port] [address]");
    }

}