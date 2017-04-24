package chat_app.server;

/**
 * Runs with:
 * <p>> java ServerEntity [port]</p>
 * If the port is not specified 1500 is used
 */
public class ServerMain {

    /**
     * @see ServerMain
     */
    public static void main(String[] args) {
        // default port
        int portNumber = 1500;

        switch (args.length) {
            case 1: // with port
                try {
                    portNumber = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid port number.");
                    return;
                }
            case 0: // default port
                break;
            default:
                usage();
                return;
        }

        // create a server object and start it
        ServerEntity server = new ServerEntity(portNumber);
        server.start();
    }

    public static void usage() {
        System.out.println("Server usage: > java Server [port]");
    }

}
