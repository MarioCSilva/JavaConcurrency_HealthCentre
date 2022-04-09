package HC.Logger;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.locks.ReentrantLock;

public class MLog implements ILog_ClientHandler {
    private final String fileName = "log.txt";
    private ReentrantLock rl;

    public MLog() {
        this.rl = new ReentrantLock();
        new File(this.fileName);
    }

    public void write(String message){
        try {
            rl.lock();

            System.out.println(message);
            FileWriter myWriter = new FileWriter(this.fileName, true);
            myWriter.write(message);
            myWriter.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } finally {
            rl.unlock();
        }
    }
}