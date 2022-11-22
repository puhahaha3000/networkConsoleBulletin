package common;

import java.io.*;

public class FileController {
    private final File file;

    public FileController(String filePath) {
        file = new File(filePath);
    }

    private void create() {
        try {
            if (file.createNewFile()) {
                System.out.println("Create File Success");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void write(String str) {
        if (!file.exists()) create();
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(str.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String read() {
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    return sb.toString();
                } else {
                    sb.append(line);
                }
            }
        } catch (FileNotFoundException e) {
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
