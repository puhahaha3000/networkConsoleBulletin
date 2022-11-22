package network;

import bulletin.Bulletin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            System.out.println("Server is running...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] : Connected");

                Bulletin bulletin = new Bulletin(socket);
                bulletin.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

