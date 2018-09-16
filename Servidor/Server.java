package Servidor;

import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private static ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(13267);

            while (true) {
                System.out.println("To on meu bem!");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Clientela chego");
                Thread thread = new Thread(new NovaConexao(clientSocket));
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}