import javax.imageio.IIOException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;
    private Map<String, String> commonDictionary = new HashMap<>();

    private int nrclients = 0;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public int getNrclients() {
        return nrclients;
    }

    public void startServer() {
        try{
            while(!serverSocket.isClosed())
            {
                Socket socket =  serverSocket.accept();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = now.format(formatter);

                System.out.println(formattedDateTime + ": A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                nrclients++;

                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e){
            closeServerSocket();
        }
    }
    public void closeServerSocket(){
        try{
            if(serverSocket!=null){
                serverSocket.close();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Serverul ruleaza pe adresa IP: " + InetAddress.getLocalHost().getHostAddress() + ", portul: 12345");
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
