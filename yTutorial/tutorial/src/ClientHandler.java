import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {

    private static final Map<String, String> dictionary;
    static {
        dictionary = new HashMap<>();
    }

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadCastMessage("SERVER: " + clientUsername + " has entered the chat");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadCastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadCastMessage(String messageToSend) {
        if ("menu".equals(messageToSend)) {
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (clientHandler.clientUsername.equals(clientUsername)) {
                        clientHandler.bufferedWriter.write(
                                "Alege una dintre optiuni:\n" +
                                        "1. Adauga cuvant\n" +
                                        "2. Modifica cuvant\n" +
                                        "3. Sterge cuvant\n" +
                                        "4. Cauta cuvant\n" +
                                        "0. Iesire din meniu");
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        } else if (messageToSend.startsWith("menu option:")) {
            try {
                int choice = Integer.parseInt(messageToSend.substring(messageToSend.length() - 1));
                switch (choice) {
                    case 1:
                        addWord();
                        break;
                    case 2:
                        editWord();
                        break;
                    case 3:
                        deleteWord();
                        break;
                    case 4:
                        searchWord();
                        break;
                    case 0:
                        bufferedWriter.write("Ieșire din meniu.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        return;
                    default:
                        bufferedWriter.write("Opțiune invalida. Reincearca.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                }
            } catch (NumberFormatException | IOException e) {
                System.out.println("Wrong choice");
                e.printStackTrace();
            }
        } else {
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (!clientHandler.clientUsername.equals(clientUsername)) {
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }
    }

    private void addWord() {
        try {
            // Trimite mesajul către client pentru a introduce cuvântul
            bufferedWriter.write("Introdu cuvantul: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește cuvântul de la client
            String word = bufferedReader.readLine();
            if (dictionary.containsKey(word)) {
                bufferedWriter.write("Cuvantul deja exista in dictionar");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {

                // Trimite mesajul către client pentru a introduce definiția cuvântului
                bufferedWriter.write("Introdu definitia cuvantului: ");
                bufferedWriter.newLine();
                bufferedWriter.flush();

                // Primește definiția de la client
                String def = bufferedReader.readLine();

                // Poți continua și să citești definiția, sau să o primești de la utilizator aici
                dictionary.put(word, def);
            }

            // Trimite serverului cererea de adăugare
            // socket.getOutputStream().write(...);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editWord() {
        try {
            // Trimite mesajul către client pentru a introduce cuvântul de modificat
            bufferedWriter.write("Introdu cuvantul pe care doresti sa-l modifici: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește cuvântul de la client
            String word = bufferedReader.readLine();

            // Poți continua și să citești noua definiție aici
            if (dictionary.containsKey(word)) {
                // Trimite mesajul către client pentru a introduce noua definiție a cuvântului
                bufferedWriter.write("Introdu noua definitie a cuvantului: ");
                bufferedWriter.newLine();
                bufferedWriter.flush();

                // Primește noua definiție de la client
                String newDef = bufferedReader.readLine();
                dictionary.put(word, newDef);
            } else bufferedWriter.write("Cuvantul nu exista in dictionar");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Trimite serverului cererea de modificare
            // socket.getOutputStream().write(...);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteWord() {
        try {
            // Trimite mesajul către client pentru a introduce cuvântul de șters
            bufferedWriter.write("Introdu cuvantul pe care doresti sa-l stergi: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește cuvântul de la client
            String word = bufferedReader.readLine();

            if (dictionary.containsKey(word)) {
                dictionary.remove(word);
            } else bufferedWriter.write("Cuvantul nu exista in dictionar");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Trimite serverului cererea de ștergere
            // socket.getOutputStream().write(...);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchWord() {
        try {
            // Trimite mesajul către client pentru a introduce cuvântul de căutat
            bufferedWriter.write("Introdu cuvantul pe care doresti sa-l cauti: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Primește cuvântul de la client
            String word = bufferedReader.readLine();

            // Trimite mesajul către client cu definiția cuvântului
            if (dictionary.get(word) == null)
                bufferedWriter.write("Nu exista acel cuvant in dictionar");
            else bufferedWriter.write("Definitia este: " + dictionary.get(word));
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Trimite serverului cererea de căutare
            // socket.getOutputStream().write(...);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadCastMessage("SERVER: " + clientUsername + " has left the chat");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        System.out.println("SERVER LOG: " + formattedDateTime + " - " + clientUsername + " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
