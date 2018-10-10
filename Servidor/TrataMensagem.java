package Servidor;

import comum.Mensagem;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;


public class TrataMensagem {
    private static byte[] mybytearray;
    private static String nomeUsuario = "";
    private static String nomeCaminho = "";
    private static String md5ArquivoCliente;
    private static String md5ArquivoServidor;

    static void tratador(Mensagem message, InputStream in, ObjectOutputStream enviaParaSocket, OutputStream out) {
        switch (message.getCommand()) {
            case ("upload"):
                OutputStream output;
                String nomeArquivo = (String) message.getArguments().get(0); //Recupera nome do arquivo.
                int tamanhoArquivo = (int) message.getArguments().get(1); //Define tamanho do Arquivo.
                nomeUsuario = (String) message.getArguments().get(2); //Recupera nome para pasta.
                md5ArquivoCliente = (String) message.getArguments().get(3); //Recupera md5 Arquivo para comparar.
                verificaExistenciaPasta(nomeUsuario);
                verificaExistenciaArquivo(nomeArquivo, nomeUsuario);
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
                    md5ArquivoServidor = criaMD5((nomeUsuario + "/" + nomeArquivo));
                    System.out.println("MD5 CLIENTE" + md5ArquivoCliente);
                    System.out.println("MD5 SERVER" + md5ArquivoServidor);
                    System.out.println(verificaIntegridadeArquivo(md5ArquivoCliente, md5ArquivoServidor));
                    if (!verificaIntegridadeArquivo(md5ArquivoCliente, md5ArquivoServidor)) {
                        verificaExistenciaArquivo(nomeArquivo, nomeUsuario); //Exclui arquivo ruim
                    }
                    output.flush();
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("recebi o arquivo: " + nomeArquivo + "de tamanho: " + tamanhoArquivo);
                break;
            case ("listaArquivos"):
                nomeCaminho = (String) message.getArguments().get(0);
                String[] nomesArquivo = recuperaListaArquivos(nomeCaminho, enviaParaSocket);
                enviarListaArquivos(nomesArquivo, enviaParaSocket);
                System.out.println("Enviei lista arquivos.");
                break;
            case ("download"):
                String arquivoDownload = (String) message.getArguments().get(0);
                nomeCaminho = (String) message.getArguments().get(1);

                if (carregaArquivo(arquivoDownload, nomeCaminho, enviaParaSocket)) {
                    enviarDadosArquivo(arquivoDownload, mybytearray.length, enviaParaSocket);
                    try {
                        out.write(mybytearray, 0, mybytearray.length);
                        out.flush();
                    } catch (IOException e) {
                        System.out.println("Não foi possível enviar arquivo");
                    }
                }
                break;
        }
    }

    private static boolean verificaIntegridadeArquivo(String md5ArquivoCliente, String md5ArquivoServidor) {
        if (md5ArquivoCliente.toUpperCase().equals(md5ArquivoServidor.toUpperCase()))
            return true;
        return false;
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

    private static String[] recuperaListaArquivos(String nomePasta, ObjectOutputStream enviaParaSocket) {
        File diretorio = new File(nomePasta);
        File files[] = diretorio.listFiles();
        try {
            String[] nomesArquivo = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                nomesArquivo[i] = files[i].getName();
            }
            return nomesArquivo;
        } catch (Exception e) {
            enviaMensagemErro("Não foi possível ler os arquivos. Tem certeza que o usuário foi digitado corretamente?", enviaParaSocket);
        }
        return new String[0];
    }


    private static void enviaMensagemErro(String mensagemErro, ObjectOutputStream enviaParaSocket) {
        //Escreve mensagem para o upload, contendo o nome do arquivo, tamanho e usuário para gravar a pasta.
        Mensagem mensagem = new Mensagem("erro", mensagemErro);
        try {
            enviaParaSocket.writeObject(mensagem);
            enviaParaSocket.flush();
        } catch (IOException e) {
            System.out.println("Não foi possível enviar dados do arquivo");
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

    private static boolean carregaArquivo(String nomeArquivo, String nomeUsuário, ObjectOutputStream enviaParaSocket) {
        try {
            //Send file
            File arquivo = new File(nomeUsuário + "/" + nomeArquivo);

            if (arquivo.isDirectory()) {
                enviaMensagemErro("Não é possível baixar uma pasta.", enviaParaSocket);
                return false;
            }

            mybytearray = new byte[(int) arquivo.length()];

            //Tenta ler arquivo
            FileInputStream fis = new FileInputStream(arquivo);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            return true;
        } catch (IOException e) {
            System.out.println("Não foi possível carregar arquivo"); //Enviar erro ao cliente
            return false;
        }
    }

    private static void enviarDadosArquivo(String nomeArquivo, int tamanhoArquivo, ObjectOutputStream dos) {
        //Escreve mensagem para o upload, contendo o nome do arquivo, tamanho e usuário para gravar a pasta.
        Mensagem mensagem = new Mensagem("dadosArquivo", nomeArquivo, tamanhoArquivo, criaMD5((nomeCaminho + "/" + nomeArquivo)));
        try {
            dos.writeObject(mensagem);
            dos.flush();
        } catch (IOException e) {
            System.out.println("Não foi possível enviar dados do arquivo");
        }
    }

    private static String criaMD5(String nomeArquivo) {
        String md5Criada = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(Paths.get(nomeArquivo)));
            byte[] digest = md.digest();
            md5Criada = DatatypeConverter.printHexBinary(digest);
            return md5Criada;
        } catch (Exception e) {
            System.out.println("Não foi possível criar MD5 do arquivo.");
            return md5Criada; //Retorna nada
        }
    }
}