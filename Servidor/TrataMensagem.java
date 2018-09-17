package Servidor;

import comum.Mensagem;

import java.io.*;


public class TrataMensagem {

    static void tratador(Mensagem message, InputStream in, ObjectOutputStream enviaParaSocket) {
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
            case ("listaArquivos"):
                String nomeUsuario = (String) message.getArguments().get(0);
                String[] nomesArquivo = recuperaListaArquivos(nomeUsuario);
                enviarListaArquivos(nomesArquivo, enviaParaSocket);
                break;
        }
    }

    private static void enviarListaArquivos(String[] nomesArquivo, ObjectOutputStream enviarParaSocket) {
        Mensagem mensagem = new Mensagem("listaArquivos", nomesArquivo);
        try {
            enviarParaSocket.writeObject(mensagem);
        } catch (IOException e) {
            System.out.println("Não foi possível enviar dados do arquivo");
        }
    }

    private static String[] recuperaListaArquivos(String nomeUsuario) {
        File diretorio = new File(nomeUsuario);
        File files[] = diretorio.listFiles();
        String[] nomesArquivo = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            nomesArquivo[i] = files[i].getName();
        }
        return nomesArquivo;
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
