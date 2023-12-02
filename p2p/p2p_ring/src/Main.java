package p2p.p2p_ring.src;

import java.util.Scanner;

public class Main {
    public static boolean isTerminated = false;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please specify the listening port number: ");
        int port = Integer.parseInt(scanner.nextLine());

        try {
            new ReadThread(port).start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.print("Please specify the remote host address: ");
        String hostname = scanner.nextLine();
        System.out.print("Please specify the remote port number: ");
        int remotePort = scanner.nextInt();

        System.out.println("Type in 'exit' and then any other input to terminate the connection.");

        try {
            new WriteThread(hostname, remotePort).start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}