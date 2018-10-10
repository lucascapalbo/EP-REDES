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

        boolean connected = false;
        int count = 0;
        System.out.println("Tentando criar conexão ao servidor.");
        while (!connected) {
            try {
                String ip = "localhost";
//            String ip = "52.207.182.18";
                int porta = 13267;
//            int porta = 44572;
                sock = new Socket(ip, porta);
                System.out.println("Conexão criada!");

                out = sock.getOutputStream(); //cria conexão de envio
                in = sock.getInputStream(); //cria conexão de recebimento;

                enviaObjeto = new ObjectOutputStream(out);
                recebeObjeto = new ObjectInputStream(in);

                connected = true;

            } catch (SocketException e) {
                if (count == 0) {
                    System.out.println("Ops, o servidor não está online.");
                    System.out.println("Tentando reconexao");
                    count++;
                }
            }
        }

        while (!exit) {
            System.out.println("O que deseja fazer?");
            System.out.println("1- Upload");
            System.out.println("2- Download");
            System.out.println("3- Trocar usuário");
            System.out.println("4- Deletar Arquivo");
            System.out.println("5- Sair");

            String opcaoEscolhida = scan.nextLine();
            switch (opcaoEscolhida.toLowerCase()) {
                case ("1"):
                    //Upload
                    System.out.println("Para retornar ao menu, digite: voltar");
                    System.out.println("Por favor, insira o nome do arquivo:");

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
                            pb.stepBy(buffer);
                        }
                        pb.close();
                    } catch (SocketException e) {
                        System.out.println("Oh oh, conexão caiu.");
                    }
                    out.flush();
                    enviaObjeto.flush();
                    break;
                case ("2"):
//                    Download
                    diretorioDownload = System.getProperty("user.home");
                    System.out.println("Para retornar ao menu, digite: voltar");
                    System.out.println("Por favor, digite o camino da pasta onde o arquivo deve ser baixado:");
                    String diretorioEscolhido = scan.nextLine();

                    Boolean diretorioExiste = false;
                    while (!diretorioExiste) {
                        if (diretorioEscolhido.toLowerCase().equals("voltar")) {
                            break;
                        }
                        if (diretorioEscolhido != "") {
                            File caminhoDownload = new File(diretorioEscolhido);
                            if (caminhoDownload.exists() && caminhoDownload.isDirectory()) {
                                diretorioDownload = diretorioEscolhido;
                                System.out.println("Caminho definido para: " + diretorioDownload);
                                diretorioExiste = true;
                            } else {
                                System.out.println("Pasta não existe, por favor digite outra pasta:");
                                diretorioEscolhido = scan.nextLine();
                            }
                        }
                    }
                    if (diretorioEscolhido.toLowerCase().equals("voltar")) {
                        break;
                    }
                    System.out.println("Estes são os arquivos que voce colocou no servidor:");

                    ArrayList nomesArquivos = recuperaListaArquivos(nomeUsuário, enviaObjeto, recebeObjeto);
                    if (nomesArquivos == null) break;
                    for (int i = 0; i < nomesArquivos.size(); i++) {
                        System.out.print(nomesArquivos.get(i) + " , ");
                    }
                    boolean arquivoSelecionado = false;
                    while (!arquivoSelecionado) {
                        //verifica o que ele quer fazer com os arquivos
                        System.out.println("Qual destes arquivos deseja baixar?");
                        String arquivoDownload = scan.nextLine();
                        if (arquivoDownload.toLowerCase().equals("voltar")) {
                            break;
                        }
                        if (validaNomeArquivo(nomesArquivos, arquivoDownload)) {
                            //Arquivo existe
                            baixaArquivo(arquivoDownload, enviaObjeto, recebeObjeto);
                            arquivoSelecionado = true;
                        } else {
                            System.out.println("Por favor, digite um dos arquivos disponíveis: ");
                        }
                    }
                    break;
                case ("3"):
                    //Trocar nome do usuário
                    System.out.println("Insira o novo usuário:");
                    nomeUsuário = scan.nextLine();
                    System.out.println("Agora está logado como: " + nomeUsuário);
                    break;

                case ("4"):
                    //Deletar Arquivo
                    System.out.println("Para retornar ao menu, digite: voltar");
                    System.out.println("Estes são os arquivos que voce colocou no servidor:");

                    ArrayList arquivosDelecao = recuperaListaArquivos(nomeUsuário, enviaObjeto, recebeObjeto);
                    if (arquivosDelecao == null) break;
                    for (int i = 0; i < arquivosDelecao.size(); i++) {
                        System.out.print(arquivosDelecao.get(i) + " , ");
                    }
                    //verifica o que ele quer fazer com os arquivos
                    System.out.println("Qual destes arquivos deseja baixar?");
                    Boolean arquivoDelecaoSelecionado = false;
                    while (!arquivoDelecaoSelecionado) {
                        String arquivoParaDeletar = scan.nextLine();
                        if (arquivoParaDeletar.toLowerCase().equals("voltar")) {
                            break;
                        }
                        if (validaNomeArquivo(arquivosDelecao, arquivoParaDeletar)) {
                            //Arquivo existe
                            deletarArquivo(arquivoParaDeletar, enviaObjeto, recebeObjeto);
                            arquivoDelecaoSelecionado = true;
                        } else {
                            System.out.println("Por favor, digite um dos arquivos disponíveis: ");
                        }
                    }
                    break;
                case ("5"):
                    //sair
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

    private static void deletarArquivo(String arquivoParaDeletar, ObjectOutputStream enviaObjeto, ObjectInputStream recebeObjeto) {
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
            } else if (dadosArquivoDownload.getCommand().equals("erro")) {
                System.out.println("Não é possível baixar arquivo, possivelmente ele é uma pasta. Por favor escolha outro");
                return;
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
                output.write(buffer, 0, count);
                if (count >= 0) controlador += count;
                pb.stepBy(count);
            }
            String md5ArquivoCliente = criaMD5(diretorioDownload + "/" + arquivoDownload);

            if (verificaIntegridadeArquivo(md5ArquivoCliente, md5ArquivoServidor)) {
                //Arquivo ruim, vou deletar
                File deletarArquivo = new File(diretorioDownload + "/" + arquivoDownload);
                if (deletarArquivo.exists()) {
                    deletarArquivo.delete();
                    System.out.println("Deletando o arquivo pois estava corrompido.");
                }
            }
            pb.close();
            System.out.println();
            output.close();
            return;
        } catch (Exception e) {
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
            } else if (listaArquivos.getCommand().equals("erro")) {
                String mensagemErro = (String) listaArquivos.getArguments().get(0);
                System.out.println(mensagemErro);
                return null;
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
            if (arquivo.isDirectory()) {
            } else {
                mybytearray = new byte[(int) arquivo.length()];
                //Tenta ler arquivo
                FileInputStream fis = new FileInputStream(arquivo);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(mybytearray, 0, mybytearray.length);
            }
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