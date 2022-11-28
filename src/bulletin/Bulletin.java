package bulletin;

import common.IoController;
import common.FileController;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

//여러명의 클라이언트에게 서버에 저장되어있는 게시판과 상호작용할 수 있도록 thread로 처리
public class Bulletin extends Thread {
    //서버의 파일에 저장된 마지막 게시판 정보를 연결함
    private static final FileController fileController = new FileController(Constant.FILE_PATH);
    //linkedList로 게시판 생성 후 정보를 불러옴
    private static final LinkedList<Record> recordList = Record.parseFromString(fileController.read());
    //게시글의 content의 입력 종료를 확인
    private static final String ESCAPE_STRING = "/q";
    //명령키입력취소
    private static final String BACK_STRING = "/b";

    //사용자가 게시판 이용을 종료하려하는지 확인
    boolean runFlag = true;
    private final IoController ioController;
    private final Socket socket;

    //소켓을 통하여 클라이언트와 서버의 상호작용
    public Bulletin(Socket socket) {
        this.socket = socket;
        ioController = new IoController(socket);
    }

    @Override
    public void run() {
        try {
            //시작멘트
            showIntro();
            //메뉴
            Command command;
            while (runFlag) {
                try {
                    //메뉴 선택 및 실행 (종료커멘드가 실행되면 runFlag가 false로 변경되어 while문 탈출)
                    command = inputCommand();
                    executeCommand(command);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            //종료맨트 및 접속해제
            showClose();
            ioController.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //게시글 추가, 삭제시 서버 파일에 저장
    private static void saveRecord() {
        fileController.write(Record.parseToString(recordList));
    }

    //종료멘트
    private void showClose() throws IOException {
        ioController.sendMsg("Program End");
        ioController.newLine();
    }

    //메뉴 실행
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

    //게시글 추가
    private void createRecord() throws IOException {
        ioController.sendMsg("Create Record... To return to the beginning, press " + BACK_STRING);
        ioController.newLine();
        String title = ioController.nextLine("Please input a title");
        if (title.equals(BACK_STRING)) return;
        //여러줄 읽기 가능 q로 입력종료
        String content = ioController.readMultipleLine("Please input a contents.\n" + "Press '" + ESCAPE_STRING + "' to complete input.\n", ESCAPE_STRING);
        if (content.equals(BACK_STRING)) return;
        String author = ioController.nextLine("Please input author");
        if (author.equals(BACK_STRING)) return;
        createRecord(title, content, author);
        ioController.sendMsg("Record created");
        ioController.newLine();
    }

    //게시글 추가 후 목록 반영(오버라이딩)
    private synchronized static void createRecord(String title, String content, String author) {
        recordList.add(new Record(title, content, author));
    }

    //게시글 삭제
    private void deleteRecord() throws IOException {
        Record record = searchRecord();
        if (record == null) return;
        deleteRecord(record);
        ioController.sendMsg("Record Deleted.");
        ioController.newLine();
    }

    //게시글 삭제 후 목록 반영(오버라이딩)
    private synchronized static void deleteRecord(Record record) {
        recordList.remove(record);
    }

    private void showDetail() throws IOException {
        Record selectedRecord = searchRecord();
        if (selectedRecord == null) return;
        ioController.sendMsg(selectedRecord.getDetailString());
    }

    //게시글 검색
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

    //게시글 목록 조회
    private void showList() throws IOException {
        ioController.sendMsg(Constant.LIST_SEPARATOR);
        ioController.sendMsg(String.format(Constant.LINE_FORMATTER, "No", "Title", "Contents", "Author", "Date"));
        for (Record record : recordList) {
            ioController.sendMsg(record.getShortString());
        }
        ioController.sendMsg(Constant.LIST_SEPARATOR);
    }

    //도움말기능
    private void showHelp() throws IOException {
        ioController.sendMsg("=== Command List ===");
        ioController.newLine();
        for (Command command : Command.values()) {
            ioController.sendMsg(String.format("%d. %s", command.getNum(), command.getName()));
            ioController.newLine();
        }
    }

    //메뉴기능
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

    //시작멘트
    private void showIntro() throws IOException {
        ioController.sendMsg("Welcome to Bulletin Program. Press '" + Command.HELP.getNum() + "' to show Help");
        ioController.newLine();
    }
}
