package Servidor;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(57291);
            while (true) {
                System.out.println("Servidor online, esperando clientes!");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectou");
                Thread thread = new Thread(new NovaConexao(clientSocket));
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}