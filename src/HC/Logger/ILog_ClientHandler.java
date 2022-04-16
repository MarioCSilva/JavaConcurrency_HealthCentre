package HC.Controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.Condition;

public interface ILog_ClientHandler {
    void write(String msg) throws IOException;
    void writeHeaders() throws IOException;
    void writeState(String msg) throws IOException;

    void suspendSimulation() throws IOException;

    void resumeSimulation() throws IOException;

    void stopSimulation() throws InterruptedException, FileNotFoundException, IOException;

    void startSimulation() throws InterruptedException, IOException;
}
