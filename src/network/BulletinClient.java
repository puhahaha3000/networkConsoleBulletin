package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class BulletinClient {
    public static void main(String[] args) {
        try {
            String serverIp = "192.168.0.88";

            // 소켓을 생성하여 연결을 요청한다.
            Socket socket = new Socket(serverIp, 7777);

            System.out.println("서버에 연결되었습니다.");
            Sender sender = new Sender(socket);
            Receiver receiver = new Receiver(socket);

            sender.start();
            receiver.start();
        } catch (Exception ce) {
            ce.printStackTrace();
        }
    } // main
} // class


class Sender extends Thread {
    DataOutputStream out;

    public Sender(Socket socket) {
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ignored) {
        }
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(out != null) {
            try {
                out.writeUTF(scanner.nextLine());
            } catch (Exception ignored) {
            }
        }
    }
}

class Receiver extends Thread {
    DataInputStream in;
    Receiver(Socket socket) {
        try {
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException ignored) {
        }

    }

    public void run() {
        while (in != null) {
            try {
                System.out.print(in.readUTF());
            } catch (IOException ignored) {
            }
        }
    } // run
}