package HC.Controller;

import HC.Communication.Message;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public interface IController_ClientHandler {
    void write(String msg) throws IOException;

    void writeHeaders() throws IOException;

    void writeState(String msg) throws IOException;

    void suspendSimulation() throws IOException;

    void resumeSimulation() throws IOException;

    void stopSimulation() throws InterruptedException, FileNotFoundException, IOException;

    void startSimulation(Message msg) throws InterruptedException, IOException;

    void endSimulation() throws IOException;

    void changeOperatingMode();

    void movePatient();

    ReentrantLock getCCHLock();
}
