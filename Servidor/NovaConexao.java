package Servidor;

import comum.Mensagem;

import java.io.*;
import java.net.Socket;

public class NovaConexao implements Runnable {
    private Socket clientSocket;

    public NovaConexao(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream in = clientSocket.getInputStream(); //Cria conexao do cliente
            OutputStream out = clientSocket.getOutputStream();
            ObjectInputStream objetoRecebido = new ObjectInputStream(in);
            ObjectOutputStream enviarObjeto = new ObjectOutputStream(out);
            while (true) {
                TrataMensagem.tratador(readMessage(objetoRecebido), in, enviarObjeto, out); //Envia Mensagem e socket para tratamento.
            }
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
        } catch (EOFException e) {
            throw new RuntimeException("Cliente fechou conexão");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Não foi possível ler objeto.");
        }
    }
}