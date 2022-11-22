package bulletin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class Record {
    private static final String LINE_SEPARATOR = "%%";
    private static final String FIELD_SEPARATOR = "##";
    private static int cnt = 1;

    private final int no;
    private final String title;
    private final String content;
    private final String author;
    private final Date createdDate;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy.MM.dd hh:mm");

    public static String parseToString(LinkedList<Record> recordLinkedList) {
        StringBuilder sb = new StringBuilder();
        for (Record record : recordLinkedList) {
            sb.append(record.no).append(FIELD_SEPARATOR);
            sb.append(record.title).append(FIELD_SEPARATOR);
            sb.append(record.content).append(FIELD_SEPARATOR);
            sb.append(record.author).append(FIELD_SEPARATOR);
            sb.append(record.createdDate.getTime()).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    public static LinkedList<Record> parseFromString(String str) {
        LinkedList<Record> recordLinkedList = new LinkedList<>();
        if (str.isEmpty()) return recordLinkedList;
        String[] lineArr = str.split(LINE_SEPARATOR);

        for (String line : lineArr) {
            String[] fieldArr = line.split(FIELD_SEPARATOR);
            recordLinkedList.add(
                    new Record(
                            Integer.parseInt(fieldArr[0]),
                            fieldArr[1],
                            fieldArr[2],
                            fieldArr[3],
                            new Date(Long.parseLong(fieldArr[4]))
                    )
            );
            cnt = Integer.parseInt(fieldArr[0]) + 1;
        }

        return recordLinkedList;
    }

    public String getShortString() {
        return String.format("| %15s | %15s | %15s | %15s | %15s |\n",
                String.format("%.15s", no),
                String.format("%.15s", title),
                String.format("%.15s", content.replace('\n', ' ')),
                String.format("%.15s", author),
                simpleDateFormat.format(createdDate));
    }

    public String getDetailString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------------------------------\n");
        sb.append("Title : ").append(title).append('\n');
        sb.append("Author : ").append(author).append('\n');
        sb.append("Date : ").append(simpleDateFormat.format(createdDate)).append('\n');
        sb.append("-------------------------------------------------------\n");
        int idx = 0;
        while (idx < content.length()) {
            sb.append(content, idx, Math.min(content.length(), idx + 55)).append('\n');
            idx += 55;
        }
        sb.append("-------------------------------------------------------\n");
        return sb.toString();
    }

    public int getNo() {
        return no;
    }

    private Record(int no, String title, String content, String author, Date createdDate) {
        this.no = no;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdDate = createdDate;
    }

    public Record(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        no = cnt++;
        Calendar calendar = Calendar.getInstance();
        createdDate = calendar.getTime();
    }
}
