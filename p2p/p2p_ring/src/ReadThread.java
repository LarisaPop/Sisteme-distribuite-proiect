package p2p.p2p_ring.src;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A thread that handles messages incoming at the listening port.
 */
public class ReadThread extends Thread {
    private final int port;

    public ReadThread(int port){ this.port = port; }

    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {

            Socket socket = serverSocket.accept();

            String remoteIP = socket.getInetAddress().getHostAddress();

            DataInputStream input;
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            String readText = "";

            while(!Main.isTerminated) {
                try {
                    readText = input.readUTF();
                    System.out.println("[" + remoteIP + "]: " + readText);

                    if(readText.equals("exit")) {
                        Main.isTerminated = true;
                    }
                } catch (IOException io) {
                    System.out.println("An error has occurred while receiving a message");
                    break;
                }
            }

            input.close();
            socket.close();

        } catch (IOException ex) {
            System.out.println("An error occurred while instantiating the listening server");
        }
    }
}