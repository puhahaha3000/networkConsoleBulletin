package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class IoController {
    private static final String ERR_MSG = "Invalid value. Repeat Please >>";
    private static final String INPUT_MARK = " >> ";
    private final DataInputStream in;
    private final DataOutputStream out;

    public IoController(Socket socket) {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            out.writeUTF("exit");
            in.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendMsg(String str) throws IOException {
        out.writeUTF(str);
    }

    public String receiveMsg() throws IOException {
        return in.readUTF();
    }

    public String nextLine(String msg) throws IOException {
        sendMsg(msg + INPUT_MARK);
        return receiveMsg();
    }

    public int nextInt(String msg) throws IOException {
        sendMsg(msg + INPUT_MARK);
        while (true) {
            try {
                return Integer.parseInt(receiveMsg());
            } catch (NumberFormatException e) {
                sendMsg(ERR_MSG);
            }
        }
    }

    public String readMultipleLine(String msg, String escapeString) throws IOException {
        sendMsg(msg + INPUT_MARK);
        StringBuilder sb = new StringBuilder();
        String line;
        while (true) {
            line = receiveMsg();
            if (line.equals(escapeString + "")) {
                return sb.toString();
            }
            sb.append(line);
            sb.append('\n');
        }
    }

    public void newLine(String s) throws IOException {
        sendMsg("\n");
    }
}