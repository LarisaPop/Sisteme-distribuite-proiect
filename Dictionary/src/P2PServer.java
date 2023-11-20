package Dictionary.src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class P2PServer {
    private static final int PORT = 5000;
    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        new P2PServer().startServer();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            // Create a list to hold the clients
            List<ClientHandler> clients = new ArrayList<>();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new thread for each connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);

                // Set the successor and predecessor clients in the ring for each client
                clients.sort(Comparator.comparingInt(c -> c.getClientSocket().getPort()));
                int index = clients.indexOf(clientHandler);

                if (index > 0) {
                    clients.get(index).setPredecessor(clients.get(index - 1));
                    clients.get(index - 1).setSuccessor(clients.get(index));
                }

                // Set the first client as the predecessor for the last client
                if (index == clients.size() - 1) {
                    clients.get(0).setPredecessor(clientHandler);
                    clientHandler.setSuccessor(clients.get(0));
                }

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Broadcast a message to all connected clients
    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Remove a disconnected client
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected: " + client.getClientSocket().getRemoteSocketAddress());

        // rest of the code...
    }
}
