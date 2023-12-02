package p2p.p2p_ring.src;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * A thread that handles sending messages to a remote host.
 */
public class WriteThread extends Thread {
    private final String hostname;
    private final int port;

    public WriteThread(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void run() {
        while (true) {
            try (Socket socket = new Socket(hostname, port)) {

                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                Scanner scanner = new Scanner(System.in);

                String input = "";

                while (!Main.isTerminated) {
                    input = scanner.nextLine();
                    output.writeUTF(input);

                    if(input.equals("exit")) {
                        Main.isTerminated = true;
                    }
                }

                break;

            } catch (IOException io) {
                System.out.println("An error has occurred. Attempting to reconnect...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}