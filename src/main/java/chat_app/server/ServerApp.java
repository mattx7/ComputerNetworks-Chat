package chat_app.server;

/**
 * Runs with:
 * <p>> java Server [port]</p>
 * If the port is not specified 1500 is used
 */
public class ServerApp {

    /**
     * @see ServerApp
     */
    public static void main(String[] args) {
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
        System.out.println("Server usage: > java Server [port]");
    }

}
