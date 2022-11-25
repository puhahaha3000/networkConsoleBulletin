package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class BulletinClient {
    public static void main(String[] args) {
        try {
            String serverIp = "127.0.0.1";

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
    Socket socket;
    public Sender(Socket socket) {
        this.socket = socket;
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ignored) {
        }
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(!socket.isClosed()) {
            try {
                out.writeUTF(scanner.nextLine());
            } catch (Exception ignored) {
            }
        }
    }
}

class Receiver extends Thread {
    DataInputStream in;
    Socket socket;
    Receiver(Socket socket) {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException ignored) {
        }

    }

    public void run() {
        while (!socket.isClosed()) {
            try {
                String str = in.readUTF();
                if (str.equals("exit")){
                    System.out.println("Press 'Enter' to exit program.");
                    socket.close();
                    break;
                } else {
                    System.out.print(str);
                }
            } catch (IOException ignored) {
            }
        }
    } // run
}