package Cliente;

import comum.Mensagem;
import comum.progressbar.ProgressBar;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;


public class ClienteSocket {
    private static Socket sock;
    private static OutputStream out;
    private static InputStream in;
    private static byte[] mybytearray;
    private static String nomeUsuário;
    private static Scanner scan;

    public static void main(String[] args) throws IOException {
        scan = new Scanner(System.in);
        System.out.println("Bem vindo ao nosso EP! Insira seu nome: ");
        nomeUsuário = scan.nextLine();

        try {
            System.out.println("Tentando criar conexão ao servidor.");
            String ip = "localhost";
            int porta = 13267;
            sock = new Socket(ip, porta);
            System.out.println("Conexão criada!");

            out = sock.getOutputStream(); //cria conexão de envio
            in = sock.getInputStream(); //cria conexão de recebimento;


        } catch (SocketException e) {
            System.out.println("Oh oh, parece que o server não ta on não.");
            return;
        }


        System.out.println("Deseja fazer download ou upload?");
        String opcaoEscolhida = scan.nextLine();

        switch (opcaoEscolhida) {
            case ("upload"):
                System.out.println("Insere o nome do arquivo ai meu bom:");
                String nomeArquivo = scan.nextLine();

                nomeArquivo = carregaArquivo(nomeArquivo);
                System.out.println("Enviando o arquivo : " + nomeArquivo);

                //Sending file name and file size to the server
                ObjectOutputStream dos = new ObjectOutputStream(out);
                enviarDadosArquivo(nomeArquivo, mybytearray.length, nomeUsuário, dos); //Envia dados do arquivo ao servidor
                System.out.println(mybytearray.length);
                dos.flush();
                ProgressBar pb = new ProgressBar("Enviando", mybytearray.length);
                try {
                    //Sending file data to the server
                    int buffer = mybytearray.length / 100;
                    int bytesLidos = 0;
                    while (bytesLidos < mybytearray.length) {
                        if (bytesLidos + buffer > mybytearray.length) {
                            buffer = mybytearray.length - bytesLidos;
                        }
                        out.write(mybytearray, bytesLidos, buffer);
                        bytesLidos += buffer;
                        //System.out.print(progresso(bytesLidos, mybytearray.length) + " ");
                        pb.stepBy(buffer);
                    }
                    pb.stop();
                } catch (SocketException e) {
                    System.out.println("Oh oh, conexão caiu.");
                }
                out.flush();
                out.close();
                dos.close();
                break;
            case ("download"):
                ObjectOutputStream enviaObjeto = new ObjectOutputStream(out);
                ObjectInputStream recebeObjeto = new ObjectInputStream(in);
                ArrayList nomesArquivos = recuperaListaArquivos(nomeUsuário, enviaObjeto, recebeObjeto);
                for (int i = 0; i < nomesArquivos.size(); i++) {
                    System.out.println(nomesArquivos.get(i));
                }
                System.out.println("Estes são os arquivos que voce pode baixar:");
                break;


        }
        //Closing socket
        sock.close();
    }

    private static ArrayList<String> recuperaListaArquivos(String nomeUsuário, ObjectOutputStream enviaObjeto, ObjectInputStream recebeLista) {
        Mensagem mensagem = new Mensagem("listaArquivos", nomeUsuário); //Recupera lista de arquivos
        try {
            enviaObjeto.writeObject(mensagem);
            Mensagem listaArquivos = (Mensagem) recebeLista.readObject(); //recupera lista do servidor
            if (listaArquivos.getCommand().equals("listaArquivos")) {
                ArrayList<String> nomesArquivos = (ArrayList) listaArquivos.getArguments();
                return nomesArquivos;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void enviarDadosArquivo(String nomeArquivo, int tamanhoArquivo, String nomeUsuário, ObjectOutputStream dos) {
        //Escreve mensagem para o upload, contendo o nome do arquivo, tamanho e usuário para gravar a pasta.
        Mensagem mensagem = new Mensagem("upload", nomeArquivo, tamanhoArquivo, nomeUsuário);
        try {
            dos.writeObject(mensagem);
        } catch (IOException e) {
            System.out.println("Não foi possível enviar dados do arquivo");
        }
    }

    private static String carregaArquivo(String nomeArquivo) {
        String nomeExatoArquivo = "";
        try {
            //Send file
            Path diretorio = Paths.get(nomeArquivo);
            nomeExatoArquivo = diretorio.getFileName().toString();
            File arquivo = new File(nomeArquivo);
            mybytearray = new byte[(int) arquivo.length()];

            //Tenta ler arquivo
            FileInputStream fis = new FileInputStream(arquivo);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
        } catch (IOException e) {
            System.out.println("Tem certeza que inseriu o nome do arquivo correto? Insira-o novamente:");
            nomeArquivo = scan.nextLine();
            carregaArquivo(nomeArquivo);
        }
        return nomeExatoArquivo;
    }
}