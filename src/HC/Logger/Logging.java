package HC.Logger;

import HC.Entities.TPatient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chico
 */
public class Logging {

    private final BufferedWriter logfile;
    private final ReentrantLock rl;
    private final Condition cWrite;
    private final ArrayList<String> headers = new ArrayList<>(Arrays.asList("STT", "ETH", "ET1", "ET2", "EVR1", "EVR2",
            "EVR3", "EVR4", "WTR1", "WTR2", "MDH", "MDR1", "MDR2", "MDR3", "MDR4", "PYH", "OUT"));

    private boolean isWriting = false;

    public Logging() throws IOException {
        this.logfile = new BufferedWriter(new FileWriter("src/main/java/HC/Logging/log.txt"));
        this.rl = new ReentrantLock();
        this.cWrite = rl.newCondition();
    }

    public void logHead() {
        log(" STT | ETH ET1 ET2 | EVR1 EVR2 EVR3 EVR4 | WTH  WTR1 WTR2 | MDH  MDR1 MDR2 MDR3 MDR4 | PYH");
    }

    public void logState(String state) {
        log(String.format(" %-4s|%-13s|%-21s|%-16s|%-26s|%-4s", state, "", "", "", "", ""));
    }

    public void logPatient(String room, TPatient patient) {
        var args = new String[headers.size()];
        Arrays.fill(args, "");
        int index = headers.indexOf(room);
        if (index == -1) {
            throw new IllegalArgumentException("Room not recognized.");
        }
        args[index] = patient.toString();
        log(String.format(" %-4s| %-4s%-4s%-4s| %-5s%-5s%-5s%-5s| %-5s%-5s%-5s| %-5s%-5s%-5s%-5s%-5s| %-4s",
                (Object[]) args));
    }

    public void log(String message) {
        try {
            rl.lock();
            while (this.isWriting) {
                cWrite.await();
            } // TODO: maybe not needed
            this.isWriting = true;
            System.out.println(message);
            this.logfile.write(message);
            this.logfile.newLine();
            this.logfile.flush();
            this.cWrite.signal();
            this.isWriting = false;
        } catch (InterruptedException | IOException ex) {
            return;
        } finally {
            rl.unlock();
        }
    }
}
