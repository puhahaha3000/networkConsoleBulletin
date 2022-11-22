package bulletin;

public enum Command {
    HELP(0, "Help"),
    LIST(1, "List"),
    CREATE(2, "Write"),
    DETAIL(3, "Read"),
    DELETE(4, "Delete"),
    QUIT(5, "Quit");

    private final int num;
    private final String name;

    Command(int num, String name) {
        this.num = num;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }
}
