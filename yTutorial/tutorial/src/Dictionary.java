import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Dictionary {
    private Map<String, String> dictionary;
    private int permissionCount; // Numărul de permisiuni necesare pentru acțiuni

    public Dictionary() {
        this.dictionary = new HashMap<>();
    }

    public synchronized boolean addWord(String word, String definition,String username, String option) {
        // Verificare permisiuni pentru adăugare cuvânt
        if (checkPermissions(username,option)) {
            dictionary.put(word, definition);
            return true;
        }
        return false;
    }

    public synchronized boolean editWord(String word, String newDefinition,String username, String option) {
        // Verificare permisiuni pentru editare cuvânt
        if (checkPermissions(username,option)) {
            if (dictionary.containsKey(word)) {
                dictionary.put(word, newDefinition);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteWord(String word,String username, String option) {
        // Verificare permisiuni pentru ștergere cuvânt
        if (checkPermissions(username,option)) {
            if (dictionary.containsKey(word)) {
                dictionary.remove(word);
                return true;
            }
        }
        return false;
    }

    public String searchWord(String word,String username, String option) {
        if(dictionary.get(word)=="null")
            return "Nu exista acel cuvant in dictionar";
        return dictionary.get(word);
    }

    /*private boolean checkPermissions() {
        // Implementează logica pentru verificarea permisiunilor
        // Returnează true dacă majoritatea clienților sunt de acord, altfel false
        return true;  // exemplu simplificat
    }*/

    private int requestPermissionFromClients(String username, String option) {
        int countYes = 0;

        for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
            try {
                String clientUsername;
                if(!clientHandler.clientUsername.equals(username)) {
                    // Trimite întrebarea către fiecare client
                    clientHandler.bufferedWriter.write("Esti de acord ca clientul " + username + " sa efectueze operatia " + option + " (da/nu)?");
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();

                    // Așteaptă răspunsul de la fiecare client
                    String response = clientHandler.bufferedReader.readLine();

                    // Dacă un client spune "da", incrementează numărul
                    if (response.trim().equalsIgnoreCase("da")) {
                        countYes++;
                    }
                }
            } catch (IOException e) {
                // Gestionarea erorilor la trimiterea sau primirea mesajelor
                e.printStackTrace();
            }
        }

        // Întoarce numărul total de "da"-uri
        return countYes;
    }

    private boolean checkPermissions(String username, String option) {
        // Obține numărul de "da"-uri de la clienți
        int countYes = requestPermissionFromClients(username, option);

        // Implementează logica pentru verificarea permisiunilor
        // De exemplu, returnează true dacă majoritatea clienților sunt de acord (de exemplu, cel puțin jumătate)
        if( countYes > ClientHandler.clientHandlers.size() / 2)
        {
            System.out.println("Se efectueaza optiunea aleasa");
            return true;
        }
        else{
            System.out.println("Nu se poate efectua optiunea");
            return false;
        }
    }
}
