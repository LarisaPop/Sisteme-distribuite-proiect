import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private Dictionary dictionary;
    private Server server;

    public Client(Socket socket, String username, Server server) {
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            this.server=server;
            this.dictionary=new Dictionary();
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage(){
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend = scanner.nextLine();
                if(messageToSend.equals("menu")){
                    startMenu(username);
                }
                else{
                    bufferedWriter.write(username + ": " + messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        }catch(IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void startMenu(String username){
        try {
            while (true) {
                // Trimite meniul către client
                bufferedWriter.write("\nAlege una dintre optiuni:");
                bufferedWriter.newLine();
                bufferedWriter.write("1. Adauga cuvant");
                bufferedWriter.newLine();
                bufferedWriter.write("2. Modifica cuvant");
                bufferedWriter.newLine();
                bufferedWriter.write("3. Sterge cuvant");
                bufferedWriter.newLine();
                bufferedWriter.write("4. Cauta cuvant");
                bufferedWriter.newLine();
                bufferedWriter.write("0. Iesire din meniu");
                bufferedWriter.newLine();
                bufferedWriter.flush();

                // Așteaptă alegerea clientului
                int choice = Integer.parseInt(bufferedReader.readLine());

                switch (choice) {
                    case 1:
                        addWord(username, "addWord");
                        break;
                    case 2:
                        editWord(username, "editWord");
                        break;
                    case 3:
                        deleteWord(username, "deleteWord");
                        break;
                    case 4:
                        searchWord(username, "searchWord");
                        break;
                    case 0:
                        bufferedWriter.write("Ieșire din meniu.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        return;
                    default:
                        bufferedWriter.write("Opțiune invalidă. Reîncearcă.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addWord(String username, String option) {
        try {
            // Trimite mesajul către client pentru a introduce cuvântul
            bufferedWriter.write("Introdu cuvantul: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește cuvântul de la client
            String word = bufferedReader.readLine();

            // Trimite mesajul către client pentru a introduce definiția cuvântului
            bufferedWriter.write("Introdu definitia cuvantului: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește definiția de la client
            String def = bufferedReader.readLine();

            // Poți continua și să citești definiția, sau să o primești de la utilizator aici
            dictionary.addWord(word, def, username, option);

            // Trimite serverului cererea de adăugare
            // socket.getOutputStream().write(...);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editWord(String username, String option) {
        try {
            // Trimite mesajul către client pentru a introduce cuvântul de modificat
            bufferedWriter.write("Introdu cuvantul pe care doresti sa-l modifici: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește cuvântul de la client
            String word = bufferedReader.readLine();

            // Trimite mesajul către client pentru a introduce noua definiție a cuvântului
            bufferedWriter.write("Introdu noua definitie a cuvantului: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește noua definiție de la client
            String newDef = bufferedReader.readLine();

            // Poți continua și să citești noua definiție aici
            dictionary.editWord(word, newDef, username, option);

            // Trimite serverului cererea de modificare
            // socket.getOutputStream().write(...);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteWord(String username, String option) {
        try {
            // Trimite mesajul către client pentru a introduce cuvântul de șters
            bufferedWriter.write("Introdu cuvantul pe care doresti sa-l stergi: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește cuvântul de la client
            String word = bufferedReader.readLine();

            dictionary.deleteWord(word, username, option);

            // Trimite serverului cererea de ștergere
            // socket.getOutputStream().write(...);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchWord(String username, String option) {
        try {
            // Trimite mesajul către client pentru a introduce cuvântul de căutat
            bufferedWriter.write("Introdu cuvantul pe care doresti sa-l cauti: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește cuvântul de la client
            String word = bufferedReader.readLine();

            // Trimite mesajul către client cu definiția cuvântului
            bufferedWriter.write("Definitia este: " + dictionary.searchWord(word, username, option));
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Trimite serverului cererea de căutare
            // socket.getOutputStream().write(...);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try {
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        String username = scanner.nextLine();

        Socket socket = new Socket("10.111.10.164", 12345);
        Client client = new Client(socket, username, null);
        client.listenForMessage();
        client.sendMessage();
    }
}
