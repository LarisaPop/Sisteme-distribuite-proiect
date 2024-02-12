import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clientHandlers = new ArrayList<>();
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {

                Dictionar.creeazaDictionar();

                Socket socket = serverSocket.accept();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = now.format(formatter);

                // Verifica daca exista deja un ClientHandler pentru acest socket
                ClientHandler existingHandler = getClientHandlerBySocket(socket);

                if (existingHandler != null) {
                    System.out.println(formattedDateTime + ": Clientul cu username-ul *" +
                            existingHandler.getClientUsername() + "* s-a reconectat");
                } else {
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clientHandlers.add(clientHandler);

                    System.out.println(formattedDateTime + ": Clientul cu username-ul *" +
                            clientHandler.getClientUsername() + "* s-a conectat");

                    Thread thread = new Thread(clientHandler);
                    thread.start();
                }
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    private ClientHandler getClientHandlerBySocket(Socket socket) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.getSocket().equals(socket)) {
                return handler;
            }
        }
        return null;
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Serverul ruleaza pe adresa IP: " +
                InetAddress.getLocalHost().getHostAddress() + ", portul: 12345");
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
