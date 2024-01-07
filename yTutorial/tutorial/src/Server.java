
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

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try{
            while(!serverSocket.isClosed())
            {
                Socket socket =  serverSocket.accept();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = now.format(formatter);

                ClientHandler clientHandler = new ClientHandler(socket);
                System.out.println(formattedDateTime + ": The client with username *" + clientHandler.clientUsername + "* has connected");

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
