package HC.Controller;

import HC.Communication.Message;
import HC.ControllerGUI.ControllerGUI;
import HC.Entities.TPatient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MController implements IController_CallCentreHall, IController_ClientHandler, IController_Patient, IController_Cashier, IController_CallCentre, IController_Nurse, IController_Doctor {
    private ReentrantLock rl;
    private BufferedWriter logFile;
    private final String fileName = "log.txt";
    private final List<String> headers;

    private final ControllerGUI controllerGUI;

    private boolean bSuspend;
    private Condition cSuspend;

    private final Condition cAwakeCC;
    private boolean bAwakeCC;

    private ReentrantLock cchLock;
    private boolean isManualMode;
    private boolean bMode;
    private Condition cMode;

    public MController(ControllerGUI controllerGUI) throws IOException {
        // default mode is automatic
        this.controllerGUI = controllerGUI;
        new File(this.fileName);
        this.logFile = new BufferedWriter(new FileWriter(fileName));

        this.rl = new ReentrantLock();
        this.cSuspend = rl.newCondition();
        this.bSuspend = false;

        this.cchLock = new ReentrantLock();
        this.cMode = this.cchLock.newCondition();
        this.isManualMode = false;
        this.bMode = false;

        this.cAwakeCC = cchLock.newCondition();
        this.bAwakeCC = false;

        this.headers = new ArrayList<>(Arrays.asList("STT", "ETH", "ET1", "ET2", "EVR1", "EVR2",
                "EVR3", "EVR4", "WTH", "WTR1", "WTR2", "MDH", "MDR1", "MDR2", "MDR3", "MDR4", "PYH", "OUT"));
    }

    public void setBAwakeCC(boolean b) {
        this.bAwakeCC = b;
    }

    public Condition getCAwakeCC() {
        return cAwakeCC;
    }

    public boolean getBAwakeCC() {
        return bAwakeCC;
    }

    public ReentrantLock getCCHLock() {
        return cchLock;
    }

    public void writeHeaders() throws IOException {
        write(" STT | ETH ET1 ET2 | EVR1 EVR2 EVR3 EVR4 | WTH  WTR1 WTR2 | MDH MDR1 MDR2 MDR3 MDR4  | PYH  | OUT");
    }

    public void writeState(String message) throws IOException {
        write(String.format(" %-4s|%-13s|%-21s|%-16s|%-26s| %-4s | %-4s", message, "", "", "", "", "", ""));
    }

    @Override
    public void suspendSimulation() throws IOException {
        rl.lock();
        bSuspend = true;
        writeState("SUS");
        rl.unlock();
    }

    @Override
    public void checkSuspend() throws InterruptedException {
        try {
            rl.lock();

            while (bSuspend)
                cSuspend.await();

        } finally {
            rl.unlock();
        }
    }

    @Override
    public void resumeSimulation() throws IOException {
        rl.lock();
        bSuspend = false;
        cSuspend.signalAll();
        writeState("RUN");
        rl.unlock();
    }

    @Override
    public void stopSimulation() throws InterruptedException, IOException {
        rl.lock();
        controllerGUI.clearGUI();
        writeState("STOP");
        bSuspend = false;
        rl.unlock();
    }

    @Override
    public void endSimulation() throws IOException {
        rl.lock();
        writeState("END");
        System.exit(0);
        rl.unlock();
    }

    public void startSimulation(Message msg) throws IOException {
        rl.lock();
        this.logFile = new BufferedWriter(new FileWriter(fileName));
        this.logFile.write("");
        this.logFile.flush();
        this.logFile.close();
        new File(this.fileName);
        this.logFile = new BufferedWriter(new FileWriter(fileName));
        write(String.format("NoA: %d, NoC: %d, NoS: %d",
                msg.getNumberOfAdults(), msg.getNumberOfChildren(), msg.getNos()));
        writeHeaders();
        writeState("INI");
        writeState("RUN");
        bSuspend = false;
        rl.unlock();
    }

    public void writePatientMovement(TPatient patient, String roomEntering) throws InterruptedException, IOException {
        var args = new String[headers.size()];
        Arrays.fill(args, "");
        int index = headers.indexOf(roomEntering);
        if (index == -1) {
            throw new IllegalArgumentException("Room not recognized.");
        }
        args[index] = patient.toString();
        String message = String.format(" %-4s| %-4s%-4s%-4s| %-5s%-5s%-5s%-5s| %-5s%-5s%-5s| %-5s%-5s%-5s%-5s%-5s| %-4s | %-4s",
                (Object[]) args);

        try {
            rl.lock();

            while (bSuspend)
                cSuspend.await();

            this.logFile.write(message);
            this.logFile.newLine();
            this.logFile.flush();

            controllerGUI.movePatient(patient, roomEntering);

        } finally {
            rl.unlock();
        }
    }

    public void write(String message) throws IOException {
        rl.lock();

        this.logFile.write(message);
        this.logFile.newLine();
        this.logFile.flush();

        rl.unlock();
    }

    public boolean checkManualMode() throws InterruptedException {
        if (isManualMode) {
            while (!bMode)
                cMode.await();
            bMode = false;
        }
        return isManualMode;
    }

    public void changeOperatingMode() {
        cchLock.lock();
        if (this.isManualMode) {

            bMode = true;
            cMode.signal();

            bAwakeCC = true;
            cAwakeCC.signal();
        }
        this.isManualMode = !this.isManualMode;
        cchLock.unlock();
    }

    public void movePatient() {
        if (this.isManualMode) {
            cchLock.lock();

            bMode = true;
            cMode.signal();

            bAwakeCC = true;
            cAwakeCC.signal();

            cchLock.unlock();
        }

    }

    public boolean getIsManualMode() {
        return isManualMode;
    }
}
