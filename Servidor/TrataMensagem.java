package Servidor;

import comum.Mensagem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class TrataMensagem {

    static void tratador(Mensagem message, InputStream in, OutputStream out) {
        switch (message.getCommand()) {
            case ("upload"):
                OutputStream output;
                String nomeArquivo = (String) message.getArguments().get(0); //Recupera nome do arquivo.
                int tamanhoArquivo = (int) message.getArguments().get(1); //Define tamanho do Arquivo.
                String nomeUsuário = (String) message.getArguments().get(2); //Recupera nome para pasta.
                verificaExistenciaPasta(nomeUsuário);
                verificaExistenciaArquivo(nomeArquivo, nomeUsuário);
                System.out.println("Vo tenta faze com esse bosta: " + nomeArquivo);
                try {
                    output = new FileOutputStream(nomeUsuário + "/" + nomeArquivo);
                    byte[] buffer = new byte[8192];
                    int count = 0;
                    while ((count = in.read(buffer)) > 0 && count <= tamanhoArquivo) {
                        output.write(buffer, 0, count);
                    }
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Closing the FileOutputStream handle
                System.out.println("recebi o arquivo: " + nomeArquivo + "de tamanho: " + tamanhoArquivo);
                break;
        }
    }

    private static void verificaExistenciaArquivo(String nomeArquivo, String nomePasta) {
        File file = new File(nomePasta + "/" + nomeArquivo); //Concatena arquivo e pasta
        if (file.exists()) {
            System.out.println("Arquivo já existe.");
            file.delete();
        }
    }

    private static void verificaExistenciaPasta(String nomePasta) {
        File dir = new File(nomePasta);
        if (dir.exists()) {
            System.out.println("Pasta já existe.");
        }
        dir.mkdir();
    }
}
