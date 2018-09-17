package Cliente;

import comum.Mensagem;
import comum.progressbar.ProgressBar;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;


public class ClienteSocket {
    private static Socket sock;
    private static OutputStream out;
    private static InputStream in;
    private static byte[] mybytearray;
    private static String nomeUsuário;
    private static Scanner scan;
    private static String diretorioDownload;
    private static boolean exit = false;
    private static String nomeExatoArquivo = "";
    private static ObjectOutputStream enviaObjeto;
    private static ObjectInputStream recebeObjeto;
    private static ProgressBar pb;
    private static String md5ArquivoServidor;

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

            enviaObjeto = new ObjectOutputStream(out);
            recebeObjeto = new ObjectInputStream(in);


        } catch (SocketException e) {
            System.out.println("Oh oh, parece que o server não ta on não.");
            return;
        }

        while (!exit) {
            System.out.println("Deseja fazer download ou upload?");
            String opcaoEscolhida = scan.nextLine();

            switch (opcaoEscolhida.toLowerCase()) {
                case ("upload"):
                    System.out.println("Insere o nome do arquivo ai meu bom:");

                    String caminhoArquivo = scan.nextLine();
                    String nomeArquivo = caminhoArquivo;

                    nomeArquivo = carregaArquivo(nomeArquivo);
                    System.out.println("Enviando o arquivo : " + nomeArquivo);

                    //Sending file name and file size to the server
                    enviarDadosArquivo(nomeArquivo, mybytearray.length, nomeUsuário, enviaObjeto, caminhoArquivo); //Envia dados do arquivo ao servidor
                    System.out.println(mybytearray.length);
                    enviaObjeto.flush();
                    pb = new ProgressBar("Enviando", mybytearray.length);
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
                        pb.close();
                    } catch (SocketException e) {
                        System.out.println("Oh oh, conexão caiu.");
                    }
                    out.flush();
                    enviaObjeto.flush();
                    break;
                case ("download"):
                    diretorioDownload = System.getProperty("user.home");
                    System.out.println("Estes são os arquivos que voce pode baixar:");

                    ArrayList nomesArquivos = recuperaListaArquivos(nomeUsuário, enviaObjeto, recebeObjeto);
                    for (int i = 0; i < nomesArquivos.size(); i++) {
                        System.out.print(nomesArquivos.get(i) + " , ");
                    }
                    System.out.println("Qual destes arquivos deseja baixar?");
                    String arquivoDownload = scan.nextLine();
                    if (validaNomeArquivo(nomesArquivos, arquivoDownload)) {
                        //Arquivo existe
                        baixaArquivo(arquivoDownload, enviaObjeto, recebeObjeto);
                    } else {
                        System.out.println("Por favor, digite um dos arquivos disponíveis: ");
                    }
                    break;
                case ("exit"):
                    exit = true;
                    System.out.println("Fechando conexão.");
                    break;
                default:
                    System.out.println("Por favor, entre um comando válido.");

            }
        }
        //Closing socket
        sock.close();
    }

    private static void baixaArquivo(String arquivoDownload, ObjectOutputStream enviaObjeto, ObjectInputStream recebeObjeto) {
        Mensagem mensagem = new Mensagem("download", arquivoDownload, nomeUsuário); //Pede arquivo para o servidor
        int tamanhoArquivoDownload = 0;
        try {
            enviaObjeto.writeObject(mensagem);
            enviaObjeto.flush();
            Mensagem dadosArquivoDownload = (Mensagem) recebeObjeto.readObject();
            if (dadosArquivoDownload.getCommand().equals("dadosArquivo")) {
                tamanhoArquivoDownload = (int) dadosArquivoDownload.getArguments().get(1);
                md5ArquivoServidor = (String) dadosArquivoDownload.getArguments().get(2);
            }
        } catch (Exception e) {
            System.out.println("Não foi possível baixar arquivo");
        }
        if (verificaExistenciaArquivo(arquivoDownload, diretorioDownload)) { //Verifica se arquivo já existe.
            System.out.println("Sobrescrevendo-o");
            //Posso nao sobrescrever.
        }
        //Lendo e gravando arquivo
        try {
            OutputStream output;
            output = new FileOutputStream(diretorioDownload + "/" + arquivoDownload);
            byte[] buffer = new byte[tamanhoArquivoDownload];
            int count = 0;
            int controlador = 0;
            pb = new ProgressBar("Baixando", tamanhoArquivoDownload);
            while ((count = in.read(buffer, 0, (tamanhoArquivoDownload - controlador))) > 0) {
                //Arrumar loop infinito
                output.write(buffer, 0, count);
                if (count >= 0) controlador += count;
                pb.stepBy(count);
            }
            String md5ArquivoCliente = criaMD5(diretorioDownload + "/" + arquivoDownload);
            verificaIntegridadeArquivo(md5ArquivoCliente, md5ArquivoServidor);
            pb.close();
            System.out.println();
            output.close();
            return;
        } catch (
                Exception e)

        {
            e.printStackTrace();
        }

    }

    private static boolean verificaIntegridadeArquivo(String md5ArquivoCliente, String md5ArquivoServidor) {
        if (md5ArquivoCliente.toUpperCase().equals(md5ArquivoServidor.toUpperCase()))
            return true;
        return false;
    }

    private static boolean validaNomeArquivo(ArrayList nomesArquivos, String arquivoDownload) {
        for (int i = 0; i < nomesArquivos.size(); i++) {
            if (arquivoDownload.equals(nomesArquivos.get(i)))
                return true;
        }
        return false;
    }

    private static ArrayList<String> recuperaListaArquivos(String nomeUsuário, ObjectOutputStream enviaObjeto, ObjectInputStream recebeLista) {
        Mensagem mensagem = new Mensagem("listaArquivos", nomeUsuário); //Recupera lista de arquivos
        try {
            enviaObjeto.writeObject(mensagem);
            enviaObjeto.flush();
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

    private static void enviarDadosArquivo(String nomeArquivo, int tamanhoArquivo, String nomeUsuário, ObjectOutputStream dos, String caminhoArquivo) {
        //Escreve mensagem para o upload, contendo o nome do arquivo, tamanho e usuário para gravar a pasta.
        Mensagem mensagem = new Mensagem("upload", nomeArquivo, tamanhoArquivo, nomeUsuário, criaMD5(caminhoArquivo));
        try {
            dos.writeObject(mensagem);
            dos.flush();
        } catch (IOException e) {
            System.out.println("Não foi possível enviar dados do arquivo");
        }
    }

    private static String carregaArquivo(String nomeArquivo) {
        nomeExatoArquivo = "";
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

    private static boolean verificaExistenciaArquivo(String nomeArquivo, String nomePasta) {
        File file = new File(nomePasta + "/" + nomeArquivo); //Concatena arquivo e pasta
        if (file.exists()) {
            System.out.println("Arquivo já existe.");
            file.delete();
            return true;
        }
        return false;
    }

    private static String criaMD5(String nomeArquivo) {
        String md5Criada = "";
        try {
            byte[] b = Files.readAllBytes(Paths.get(nomeArquivo));
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            md5Criada = DatatypeConverter.printHexBinary(hash);
            return md5Criada;
        } catch (Exception e) {
            System.out.println("Não foi possível criar MD5 do arquivo.");
            return md5Criada; //Retorna nada
        }
    }
}