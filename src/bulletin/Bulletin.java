package bulletin;

import common.IoController;
import common.FileController;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class Bulletin extends Thread {
    private static final FileController fileController = new FileController(Constant.FILE_PATH);
    private static final LinkedList<Record> recordList = Record.parseFromString(fileController.read());
    private static final String ESCAPE_STRING = "/q";
    private static final String BACK_STRING = "/b";

    boolean runFlag = true;
    private final IoController ioController;
    private final Socket socket;

    public Bulletin(Socket socket) {
        this.socket = socket;
        ioController = new IoController(socket);
    }

    @Override
    public void run() {
        try {
            showIntro();
            Command command;
            while (runFlag) {
                try {
                    command = inputCommand();
                    executeCommand(command);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            showClose();
            ioController.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveRecord() {
        fileController.write(Record.parseToString(recordList));
    }

    private void showClose() throws IOException {
        ioController.sendMsg("Program End");
        ioController.newLine();
    }

    private void executeCommand(Command command) throws IOException {
        switch (command) {
            case HELP -> showHelp();
            case LIST -> showList();
            case CREATE -> {
                createRecord();
                saveRecord();
            }
            case DETAIL -> showDetail();
            case DELETE -> {
                deleteRecord();
                saveRecord();
            }
            case QUIT -> {
                saveRecord();
                runFlag = false;
            }
        }
    }

    private void createRecord() throws IOException {
        ioController.sendMsg("Create Record... To return to the beginning, press " + BACK_STRING);
        ioController.newLine();
        String title = ioController.nextLine("Please input a title");
        if (title.equals(BACK_STRING)) return;
        String content = ioController.readMultipleLine("Please input a contents.\n" + "Press '" + ESCAPE_STRING + "' to complete input.\n", ESCAPE_STRING);
        if (content.equals(BACK_STRING)) return;
        String author = ioController.nextLine("Please input author");
        if (author.equals(BACK_STRING)) return;
        createRecord(title, content, author);
        ioController.sendMsg("Record created");
        ioController.newLine();
    }

    private synchronized static void createRecord(String title, String content, String author) {
        recordList.add(new Record(title, content, author));
    }

    private void deleteRecord() throws IOException {
        Record record = searchRecord();
        if (record == null) return;
        deleteRecord(record);
        ioController.sendMsg("Record Deleted.");
        ioController.newLine();
    }

    private synchronized static void deleteRecord(Record record) {
        recordList.remove(record);
    }

    private void showDetail() throws IOException {
        Record selectedRecord = searchRecord();
        if (selectedRecord == null) return;
        ioController.sendMsg(selectedRecord.getDetailString());
    }

    private Record searchRecord() throws IOException {
        while (true) {
            ioController.sendMsg("Select Record... To return to the beginning, press " + BACK_STRING);
            ioController.newLine();
            String inputNo = ioController.nextLine("Select a number");
            if (inputNo.equals(BACK_STRING)) return null;
            int no = Integer.parseInt(inputNo);
            Record record = recordList.stream().filter(it -> it.getNo() == no).findAny().orElse(null);
            if (record == null) {
                ioController.sendMsg("Invalid Number");
            } else {
                return record;
            }
        }
    }

    private void showList() throws IOException {
        ioController.sendMsg(Constant.LIST_SEPARATOR);
        ioController.sendMsg(String.format(Constant.LINE_FORMATTER, "No", "Title", "Contents", "Author", "Date"));
        for (Record record : recordList) {
            ioController.sendMsg(record.getShortString());
        }
        ioController.sendMsg(Constant.LIST_SEPARATOR);
    }

    private void showHelp() throws IOException {
        ioController.sendMsg("=== Command List ===");
        ioController.newLine();
        for (Command command : Command.values()) {
            ioController.sendMsg(String.format("%d. %s", command.getNum(), command.getName()));
            ioController.newLine();
        }
    }

    private Command inputCommand() throws IOException {
        while (true) {
            int input = ioController.nextInt("Please input command");
            for (Command command : Command.values()) {
                if (command.getNum() == input) {
                    return command;
                }
            }
            ioController.sendMsg("Invalid command. Input '" + Command.HELP.getNum() + "' to show Help");
        }
    }

    private void showIntro() throws IOException {
        ioController.sendMsg("Welcome to Bulletin Program. Press '" + Command.HELP.getNum() + "' to show Help");
        ioController.newLine();
    }
}
