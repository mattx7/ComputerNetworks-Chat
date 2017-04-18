package chat_app.server;

/**
 * Created by lionpierau on 18.04.17.
 */
public class ServerApp {

    /**
     * To run as a console application just open a console window and:
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        // start server on port 1500 unless a PortNumber is specified
        int portNumber = 1500;
        switch (args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    usage();
                    return;
                }
            case 0:
                break;
            default:
                usage();
                return;

        }
        // create a server object and start it
        ChatServer server = new ChatServer(portNumber);
        server.start();
    }

    public static void usage() {
        System.out.println("Usage is: > java Server [portNumber]");
    }

}
