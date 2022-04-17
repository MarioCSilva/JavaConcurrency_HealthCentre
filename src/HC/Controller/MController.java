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
    /* Name of the File where the Log Output will be written*/ 
    private final String fileName = "log.txt";
    private final List<String> headers;

    // access to controlls of the interface
    private final ControllerGUI controllerGUI;

    // flag and condition for suspending activity of threads
    private boolean bSuspend;
    private Condition cSuspend;

    // lock used in the Call Centre Hall monitor
    private ReentrantLock cchLock;
    // flag and condition of Call Centre for when he has no patients to handle
    private final Condition cAwakeCC;
    private boolean bAwakeCC;

    // flags and condition of Call Centre for when manual mode is activated
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

    
      
    /** 
     * Call the @write method to write on the Log File the Headers for all Rooms in Each Hall
     * @throws IOException
     */
    public void writeHeaders() throws IOException {
        write(" STT | ETH ET1 ET2 | EVR1 EVR2 EVR3 EVR4 | WTH  WTR1 WTR2 | MDH MDR1 MDR2 MDR3 MDR4  | PYH  | OUT");
    }

    
    /** 
     * @param message the new State to be written to the Log File(RUN or SUS, for example)
     * @throws IOException
     */
    public void writeState(String message) throws IOException {
        write(String.format(" %-4s|%-13s|%-21s|%-16s|%-26s| %-4s | %-4s", message, "", "", "", "", "", ""));
    }

    
    /** 
     * method Responsible for allowing the Suspension of the Simulation, 
     * by putting the boolean @bSuspend variable to false. 
     * Also Writes the SUS state on the log file
     * @throws IOException
     */
    @Override
    public void suspendSimulation() throws IOException {
        rl.lock();
        bSuspend = true;
        writeState("SUS");
        rl.unlock();
    }

    
    /** 
     * Method used by active entities that suspends them if the flag bSuspend is true.
     *
     * @throws InterruptedException
     */
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

    
    /** 
     * method Responsible for allowing the Resuming of the Simulation.
     * Also writes the RUN state on the log file.
     * @throws IOException
     */
    @Override
    public void resumeSimulation() throws IOException {
        rl.lock();
        bSuspend = false;
        cSuspend.signalAll();
        writeState("RUN");
        rl.unlock();
    }

    
    /** 
     * method Responsible for allowing the stoppage of the Simulation.
     * Also writes the RUN state on the log file and clears the interface of the Health Centre
     * 
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public void stopSimulation() throws InterruptedException, IOException {
        rl.lock();
        controllerGUI.clearGUI();
        writeState("STOP");
        bSuspend = false;
        rl.unlock();
    }

    
    /** 
     * method Responsible for allowing the ending of the Simulation.
     * Also writes the END state on the log file.
     * @throws IOException
     */
    @Override
    public void endSimulation() throws IOException {
        rl.lock();
        writeState("END");
        System.exit(0);
        rl.unlock();
    }

    
    /** 
     * Method Responsible for starting the simulation.
     * Also writes the Headers, and the INI and RUN States on the Log file. 
     * @param msg The Message object sent by the CC.
     * @throws IOException
     */
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

    
    /** 
     * @param patient
     * @param roomEntering
     * @throws InterruptedException
     * @throws IOException
     */
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

    
    /** 
     * @param message
     * @throws IOException
     */
    public void write(String message) throws IOException {
        rl.lock();

        this.logFile.write(message);
        this.logFile.newLine();
        this.logFile.flush();

        rl.unlock();
    }

    
    /** 
     * Method used by Call Centre to check if the operating mode has been changed to Manual Mode
     * If manual mode is activated then it awaits for a signal to be activated.
     * Returns the flag isManualMode so Call Centre can also know if it must release only one patient.
     * 
     * @return boolean
     * @throws InterruptedException
     */
    public boolean checkManualMode() throws InterruptedException {
        if (isManualMode) {
            while (!bMode)
                cMode.await();
            bMode = false;
        }
        return isManualMode;
    }
    
     /**
     * Method that uses the Call Centre Lock and signals both the conditions of Call Centre
     * so he can proceed it's work.
     */
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

 
    /** 
     *  Method used to move a Patient in Manual mode.
     * 
     */
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

    
    /** 
     * @return boolean
     */
    public boolean getIsManualMode() {
        return isManualMode;
    }

     /** 
     * Call Centre uses this method to change the flag that checks his permission to wake up
     *
     * @param b boolean value to be assigned to the @bAwakeCC Variable
     */
    public void setBAwakeCC(boolean b) {
        this.bAwakeCC = b;
    }
    
    /** 
     * 
     * @return Condition to handle the Call Centre. Useful for the Manual Mode.
     */
    public Condition getCAwakeCC() {
        return cAwakeCC;
    }

    
    /** 
     * @return boolean
     */
    public boolean getBAwakeCC() {
        return bAwakeCC;
    }

    
    /** 
     * @return ReentrantLock 
     */
    public ReentrantLock getCCHLock() {
        return cchLock;
    }

}
