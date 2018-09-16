package Servidor;

import comum.Mensagem;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NovaConexao implements Runnable {
    private Socket clientSocket;

    public NovaConexao(Socket socket) {
        System.out.println("Oi da thread");
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream in = clientSocket.getInputStream(); //Cria conexao do cliente
            OutputStream out = clientSocket.getOutputStream();
            ObjectInputStream objetoRecebido = new ObjectInputStream(in);

            TrataMensagem.tratador(readMessage(objetoRecebido), in, out); //Envia Mensagem e socket para tratamento.

            objetoRecebido.close();
            in.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Mensagem readMessage(ObjectInputStream in) {
        Mensagem input;
        try {
            while (true)
                if ((input = (Mensagem) in.readObject()) != null) {
                    return input;
                }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Não foi possível ler objeto.");
        }
    }
}

