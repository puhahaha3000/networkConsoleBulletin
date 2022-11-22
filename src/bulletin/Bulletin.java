package bulletin;

import common.IoController;
import common.FileController;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class Bulletin extends Thread {
    private static final FileController fileController = new FileController(Constant.FILE_PATH);
    private static final LinkedList<Record> recordList = Record.parseFromString(fileController.read());

    boolean runFlag = true;
    private final char ESCAPE_CHAR = 'q';
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
        String title = ioController.nextLine("Please input a title");
        String content = ioController.readMultipleLine("Please input a contents.\n" + "Press '" + ESCAPE_CHAR + "' to complete input.\n", ESCAPE_CHAR);
        String author = ioController.nextLine("Please input author");
        createRecord(title, content, author);
    }

    private synchronized static void createRecord(String title, String content, String author) {
        recordList.add(new Record(title, content, author));
    }

    private void deleteRecord() throws IOException {
        Record record = searchRecord();
        deleteRecord(record);
    }

    private synchronized static void deleteRecord(Record record) {
        recordList.remove(record);
    }

    private void showDetail() throws IOException {
        ioController.sendMsg(searchRecord().getDetailString());
    }

    private Record searchRecord() throws IOException {
        while (true) {
            int no = ioController.nextInt("Select a number");
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
        ioController.sendMsg("=== Command List ===\n");
        for (Command command : Command.values()) {
            ioController.sendMsg(String.format("%d. %s\n", command.getNum(), command.getName()));
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
        ioController.sendMsg("Welcome to Bulletin Program. Press '" + Command.HELP.getNum() + "' to show Help\n");
    }
}
