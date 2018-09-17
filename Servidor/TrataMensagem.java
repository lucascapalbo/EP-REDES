package Servidor;

import comum.Mensagem;

import java.io.*;


public class TrataMensagem {
    private static byte[] mybytearray;
    private static String nomeUsuario = "";

    static void tratador(Mensagem message, InputStream in, ObjectOutputStream enviaParaSocket, OutputStream out) {
        switch (message.getCommand()) {
            case ("upload"):
                OutputStream output;
                String nomeArquivo = (String) message.getArguments().get(0); //Recupera nome do arquivo.
                int tamanhoArquivo = (int) message.getArguments().get(1); //Define tamanho do Arquivo.
                nomeUsuario = (String) message.getArguments().get(2); //Recupera nome para pasta.
                verificaExistenciaPasta(nomeUsuario);
                verificaExistenciaArquivo(nomeArquivo, nomeUsuario);
                System.out.println("Vo tenta faze com esse bosta: " + nomeArquivo);
                try {
                    output = new FileOutputStream(nomeUsuario + "/" + nomeArquivo);
                    byte[] buffer = new byte[8192];
                    int count = 0;
                    int controlador = 0;
                    while ((count = in.read(buffer)) > 0) {
                        output.write(buffer, 0, count);
                        if (count >= 0) controlador += count;
                        if (controlador >= tamanhoArquivo) break;
                    }
                    output.flush();
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Closing the FileOutputStream handle
                System.out.println("recebi o arquivo: " + nomeArquivo + "de tamanho: " + tamanhoArquivo);
                break;
            case ("listaArquivos"):
                nomeUsuario = (String) message.getArguments().get(0);
                String[] nomesArquivo = recuperaListaArquivos(nomeUsuario);
                enviarListaArquivos(nomesArquivo, enviaParaSocket);
                System.out.println("Enviei lista arquivos.");
                break;
            case ("download"):
                String arquivoDownload = (String) message.getArguments().get(0);
                nomeUsuario = (String) message.getArguments().get(1);
                carregaArquivo(arquivoDownload, nomeUsuario);
                enviarDadosArquivo(arquivoDownload, mybytearray.length, enviaParaSocket);
                try {
                    out.write(mybytearray, 0, mybytearray.length);
                    out.flush();
                } catch (IOException e) {
                    System.out.println("Não foi possível enviar arquivo");
                }
                break;
        }
    }


    private static void enviarListaArquivos(String[] nomesArquivo, ObjectOutputStream enviarParaSocket) {
        Mensagem mensagem = new Mensagem("listaArquivos", nomesArquivo);
        try {
            enviarParaSocket.writeObject(mensagem);
            enviarParaSocket.flush();
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

    private static void carregaArquivo(String nomeArquivo, String nomeUsuário) {
        try {
            //Send file
            File arquivo = new File(nomeUsuário + "/" + nomeArquivo);
            mybytearray = new byte[(int) arquivo.length()];

            //Tenta ler arquivo
            FileInputStream fis = new FileInputStream(arquivo);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
        } catch (IOException e) {
            System.out.println("Não foi possível carregar arquivo"); //Enviar erro ao cliente
        }
    }

    private static void enviarDadosArquivo(String nomeArquivo, int tamanhoArquivo, ObjectOutputStream dos) {
        //Escreve mensagem para o upload, contendo o nome do arquivo, tamanho e usuário para gravar a pasta.
        Mensagem mensagem = new Mensagem("dadosArquivo", nomeArquivo, tamanhoArquivo);
        try {
            dos.writeObject(mensagem);
            dos.flush();
        } catch (IOException e) {
            System.out.println("Não foi possível enviar dados do arquivo");
        }
    }
}