package Dictionary.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private P2PServer server;
    private PrintWriter out;
    private ClientHandler successor;
    private ClientHandler predecessor;

    public ClientHandler(Socket clientSocket, P2PServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    // Set the successor and predecessor clients in the ring
    public void setSuccessor(ClientHandler successor) {
        this.successor = successor;
    }

    public void setPredecessor(ClientHandler predecessor) {
        this.predecessor = predecessor;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Send the received message to the successor and predecessor clients in the ring
                successor.sendMessage("Successor: " + inputLine);
                predecessor.sendMessage("Predecessor: " + inputLine);
            }

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            // Remove the client when it disconnects
            server.removeClient(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send a message to the client
    public void sendMessage(String message) {
        out.println(message);
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
