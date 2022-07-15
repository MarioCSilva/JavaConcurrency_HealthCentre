package HC.CallCentreHall;

import HC.Controller.IController_CallCentreHall;
import HC.Entities.TCallCentre;
import HC.Entities.TPatient;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MCallCentreHall implements ICallCentreHall_Patient,
        ICallCentreHall_CallCentre {
    private final ReentrantLock lock1;

    private final IController_CallCentreHall controller;
    private Condition cAwakeCC;
    // hash map of the halls and the corresponding number of patients there
    private final HashMap<String, Integer> hallPatients;

    public MCallCentreHall(int nos, IController_CallCentreHall controller) {
       
        this.hallPatients = new HashMap<>();
        //all Halls that the Call Centre will controll
        this.hallPatients.put("ETH", 0);
        this.hallPatients.put("EVH", 0);
        // In these halls its differentiated Adults(A) from Children(C) to simplify the management
        this.hallPatients.put("WTHA", 0);
        this.hallPatients.put("WTHC", 0);
        this.hallPatients.put("MDWA", 0);
        this.hallPatients.put("MDWC", 0);
        this.hallPatients.put("MDRA", 0);
        this.hallPatients.put("MDRC", 0);

        // to obtain the same lock and condition used for controlling the call centre entity
        this.controller = controller;
        this.lock1 = controller.getCCHLock();
        this.cAwakeCC = controller.getCAwakeCC();
    }

    /**
     * @param patient the Patient that notified the Call Centre of its Entrance
     * @param hall the String identifier of the Hall that the User has entered
     */
    public void notifyEntrance(TPatient patient, String hall) {
        lock1.lock();

        // complement the @hall String with the Patient type(WTHA or WTHC)
        if (hall.equals("WTH"))
            hall += patient.getPatientType();

        int patients = hallPatients.get(hall);
        // increase the number of patients by one
        hallPatients.put(hall, patients + 1);
        //wake up the Call Centre to check which patients can be called to other Halls
        this.cAwakeCC.signal();
        this.controller.setBAwakeCC(true);

        lock1.unlock();
    }

    /**
     * @param patient the Patient that notified the Call Centre of its Exit
     * @param hall the String identifier of the Hall that the User has exited
     */
    public void notifyExit(TPatient patient, String hall) {
        lock1.lock();
        // complement the @hall String with the Patient type(MDWA or MDWC)
        if (hall.equals("MDW") || hall.equals("MDR"))
            hall += patient.getPatientType();
        //decrease the number of patients since a patient has exited
        int patients = hallPatients.get(hall);
        hallPatients.put(hall, patients - 1);
        //wake up the Call Centre to check which patients can be called to other Halls
        this.cAwakeCC.signal();
        this.controller.setBAwakeCC(true);

        lock1.unlock();
    }

    /**
     * method to be called by the Call Centre when its thread is running
     * @param cc the Call Centre Instance
     * @throws InterruptedException
     */
    public void work(TCallCentre cc) throws InterruptedException {
        int numToCallETH = 0, numToCallWTHA = 0, numToCallWTHC = 0, numToCallMDWA = 0, numToCallMDWC = 0, i = 0;
        boolean isManualMode = false;
        int maxSeatsEVR = 4;        // maximum number of seats of the Evaluation Room
        int maxSeatsMDW = 1;        // maximum number of seats of the Medical Waiting Room
        int maxSeatsMDR = 2;        // maximum number of seats of the Medical Appointment Room

        while (true) {

            try {
                lock1.lock();
                // Call Centre stays blocked while has not been "woken" up
                while (!this.controller.getBAwakeCC())
                    this.cAwakeCC.await();

                this.controller.setBAwakeCC(false);

                // Call Centre stays blocked while manual mode is activated
                // returns a boolean @isManualMode that indicates if it is activated the manual mode or not
                isManualMode = cc.getController().checkManualMode();

                // total number of patients that should be called;
                int totalCalledPatients = 0;
                
                // Retrieve the number of patients that should be called to each Hall
                numToCallETH = updateNumPatients("ETH", "EVH", maxSeatsEVR,
                        totalCalledPatients, isManualMode);
                totalCalledPatients += numToCallETH;

                numToCallWTHC = updateNumPatients("WTHC", "MDWC", maxSeatsMDW,
                        totalCalledPatients, isManualMode);
                totalCalledPatients += numToCallWTHC;

                numToCallWTHA = updateNumPatients("WTHA", "MDWA", maxSeatsMDW,
                        totalCalledPatients, isManualMode);
                totalCalledPatients += numToCallWTHA;

                numToCallMDWA = updateNumPatients("MDWA", "MDRA", maxSeatsMDR,
                        totalCalledPatients, isManualMode);
                totalCalledPatients += numToCallMDWA;

                numToCallMDWC = updateNumPatients("MDWC", "MDRC", maxSeatsMDR,
                        totalCalledPatients, isManualMode);

            } finally {
                lock1.unlock();
            }

            // after releasing the lock
            // call for each room/hall the appropriate number of patients
            for (i = 0; i < numToCallETH; i++)
                cc.callETHPatient();

            for (i = 0; i < numToCallWTHA; i++)
                cc.callWTHAPatient();

            for (i = 0; i < numToCallWTHC; i++)
                cc.callWTHCPatient();

            for (i = 0; i < numToCallMDWA; i++)
                cc.callMDWAPatient();

            for (i = 0; i < numToCallMDWC; i++)
                cc.callMDWCPatient();
        }
    }

    /**
     * Method that updates the number of patients accordingly to the rooms/halls.
     * It calculates the number of seats available in the @hallToEnter, and then gets the
     * number of patients that can be called to that hall/room.
     * When Manual Mode is on, it must check if it has already been called one patient before
     * with the @totalCalledPatients parameter, if no patient has been called, and
     * it is possible to move patients from the two halls/rooms passed, then it forces the number
     * of patients to move to 1.
     * 
     * @param hallToExit the String identifier of the Hall that the Patient has left
     * @param hallToEnter the String identifier of the Hall that the Patient(s) will enter
     * @param maxSeatsHallToEnter the max number of Seats the Hall that the Patient wants to enter has
     * @param totalCalledPatients total number of patients that are going to be moved/called already prior to these halls
     * @param isManualMode boolean that checks if manualMode is On (true) or Off (false)
     * @return int the number of patients that will be called to the hall identified by halltoEnter 
     */
    public int updateNumPatients(String hallToExit, String hallToEnter, int maxSeatsHallToEnter,
            int totalCalledPatients, boolean isManualMode) {
        int numToCall = 0;
        int nToExit = hallPatients.get(hallToExit);
        int nToEnter = hallPatients.get(hallToEnter);
        
        // checks how many free Spaces are left in Enter Hall
        int freeSpaces = maxSeatsHallToEnter - nToEnter;
        // gets the number of patients that will be called
        numToCall = freeSpaces <= nToExit ? freeSpaces : nToExit;

        if (isManualMode) {
            if (totalCalledPatients == 0 && numToCall > 0) {
                numToCall = 1;
            } else {
                numToCall = 0;
            }
        }

        nToExit -= numToCall;
        nToEnter += numToCall;

        // update data structure with the number of patients in each hall/room
        if (!hallToEnter.equals("MDRA") && !hallToEnter.equals("MDRC"))
            hallPatients.put(hallToExit, nToExit);

        hallPatients.put(hallToEnter, nToEnter);

        return numToCall;
    }
}
