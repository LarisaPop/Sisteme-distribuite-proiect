import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
public class ClientHandler implements Runnable {


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
                                        "0. Iesire din meniu\n"+
                                    "ATENTIE! Dupa selectarea unei optiuni, pentru a putea fi realizata, utilizatorul va scrie de la linia de comanda \"optiune,cuvant,definitie\"");
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
                        System.out.println("Add");
                        addWord();
                        break;
                    case 2:
                        System.out.println("Edit");
                        editWord();
                        break;
                    case 3:
                        System.out.println("Delete");
                        deleteWord();
                        break;
                    case 4:
                        System.out.println("Search");
                        searchWord();
                        break;
                    case 0:
                        bufferedWriter.write("Iesire din meniu.");
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

    @SuppressWarnings("unchecked")
    private void addWord() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("dictionary.json"));
            JSONObject jsonObject = (JSONObject) obj;
            bufferedWriter.write("Introdu optiunea,cuvantul,definitia: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            String word = bufferedReader.readLine();
            String[] add=word.split(",");
            if (jsonObject.containsKey(add[1])) {
                bufferedWriter.write("Cuvantul deja exista in dictionar");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                jsonObject.put(add[1], add[2]);
                System.out.println(jsonObject);
                @SuppressWarnings("resource")
                FileWriter file = new FileWriter("dictionary.json", false);
                try {
                    file.write(jsonObject.toJSONString());
                    file.flush();
                } catch (IOException e) {
                    System.out.println("Eroare! A apărut o eroare I/O!");
                }
                bufferedWriter.write("Cuvantul a fost adaugat cu succes in dictionar.");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void editWord() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("dictionary.json"));
            JSONObject jsonObject = (JSONObject) obj;
            bufferedWriter.write("Introdu optiunea,cuvantul pe care doresti sa-l modifici,definitia: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            String word = bufferedReader.readLine();
            String[] edit=word.split(",");
            if (jsonObject.containsKey(edit[1])) {
                // Actualizează definiția cuvântului în obiectul JSON
                jsonObject.put(edit[1], edit[2]);

                @SuppressWarnings("resource")
                FileWriter file = new FileWriter("dictionary.json", false);
                try {
                    file.write(jsonObject.toJSONString());
                    file.flush();
                } catch (IOException e) {
                    System.out.println("Eroare! A apărut o eroare I/O!");
                }

                // Informează clientul că cuvântul a fost actualizat cu succes
                bufferedWriter.write("Cuvantul a fost modificat cu succes in dictionar.");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                // Informează clientul că nu s-a găsit cuvântul în dicționar
                bufferedWriter.write("Cuvantul nu exista in dictionar");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }



    private void deleteWord() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("dictionary.json"));
            JSONObject jsonObject = (JSONObject) obj;
            // Trimite mesajul către client pentru a introduce cuvântul de șters
            bufferedWriter.write("Introdu optiunea,cuvantul pe care doresti sa-l stergi: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            // Primește cuvântul de la client
            String word = bufferedReader.readLine();
            String[] delete=word.split(",");
            // Verifică dacă cuvântul există în dicționar
            if (jsonObject.containsKey(delete[1])) {
                // Elimină cuvântul din obiectul JSON
                jsonObject.remove(delete[1]);
                FileWriter file = new FileWriter("dictionary.json", false);
                try {
                    file.write(jsonObject.toJSONString());
                    file.flush();
                } catch (IOException e) {
                    System.out.println("Eroare! A apărut o eroare I/O!");
                }
                // Informează clientul că cuvântul a fost șters cu succes
                bufferedWriter.write("Cuvantul a fost sters cu succes din dictionar.");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                // Informează clientul că nu s-a găsit cuvântul în dicționar
                bufferedWriter.write("Cuvantul nu exista in dictionar");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private void searchWord() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("dictionary.json"));
            JSONObject jsonObject = (JSONObject) obj;

            // Trimite mesajul către client pentru a introduce cuvântul de căutat
            bufferedWriter.write("Introdu optiunea,cuvantul pe care doresti sa-l cauti: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            // Primește cuvântul de la client
            String word = bufferedReader.readLine();
            String[] search=word.split(",");
            // Verifică dacă cuvântul există în dicționar
            String definition = (String) jsonObject.get(search[1]);
            // Verifică dacă definiția este null (adică cuvântul nu există în dicționar)
            if (definition != null) {

                // Trimite mesajul către client cu definiția cuvântului
                bufferedWriter.write("Definitia este: " + definition);
            } else {
                // Trimite mesajul către client că cuvântul nu există în dicționar
                bufferedWriter.write("Cuvantul nu exista in dictionar");
            }
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
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

    public String getClientUsername() {
        return clientUsername;
    }

    public Object getSocket() {
        return socket;
    }
}
