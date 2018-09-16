package Servidor;

public class MainThread {
    private static Server server;

    public static void main(String[] args) {
        runServerSocket();
    }

    private static void runServerSocket() {
        server = new Server();

        Thread thread = new Thread(server);
        thread.start();

    }
}

