package HC.CallCentreHall;

import HC.Entities.TCallCentre;
import HC.Entities.TPatient;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MCallCentreHall implements ICallCentreHall_Patient,
        ICallCentreHall_CallCentre {
    private int nPatientsETH;
    private int nPatientsEVH;
    private int nPatientsWTH;
    private int nPatientsMDWAdults;
    private int nPatientsMDWChildren;
    private int nPatientsMDRAdults;
    private int nPatientsMDRChildren;
    private ReentrantLock lock1;
    private Condition cAwakeCC;
    private boolean bAwakeCC;
    private final int maxSeatsEVR = 4;
    private final int maxSeatsMDW = 1;
    private final int maxSeatsMDR = 2;

    private HashMap<String, Integer> hallPatients;

    public MCallCentreHall(int nos) {
        this.nPatientsETH = 0;
        this.nPatientsEVH = 0;
        this.nPatientsWTH = 0;

        this.nPatientsMDWAdults = 0;
        this.nPatientsMDWChildren = 0;

        this.nPatientsMDRAdults = 0;
        this.nPatientsMDRChildren = 0;

        this.hallPatients = new HashMap<>();
        this.hallPatients.put("ETH", 0);
        this.hallPatients.put("EVH", 0);
        this.hallPatients.put("WTHA", 0);
        this.hallPatients.put("WTHC", 0);
        this.hallPatients.put("MDWA", 0);
        this.hallPatients.put("MDWC", 0);
        this.hallPatients.put("MDRA", 0);
        this.hallPatients.put("MDRC", 0);

        this.lock1 = new ReentrantLock();
        this.cAwakeCC = lock1.newCondition();
        this.bAwakeCC = false;
    }


    public void notifyEntrance(TPatient patient, String hall) {
        lock1.lock();

        if (hall.equals("WTH"))
            hall += patient.getPatientType();

        int patients = hallPatients.get(hall);
        hallPatients.put(hall, patients + 1);

        this.cAwakeCC.signal();
        this.bAwakeCC = true;

        lock1.unlock();
    }

    public void notifyExit(TPatient patient, String hall) {
        lock1.lock();

        if (hall.equals("MDW") || hall.equals("MDR"))
            hall += patient.getPatientType();

        int patients = hallPatients.get(hall);
        hallPatients.put(hall, patients - 1);

        this.cAwakeCC.signal();
        this.bAwakeCC = true;

        lock1.unlock();
    }

    public void work(TCallCentre cc) throws InterruptedException {
        int numToCallETH = 0, numToCallWTHA = 0, numToCallWTHC = 0, numToCallMDWA = 0, numToCallMDWC = 0, i = 0;

        while (true) {

            try {
                lock1.lock();

                while (!this.bAwakeCC)
                    this.cAwakeCC.await();

                this.bAwakeCC = false;

                numToCallETH = updateNumPatients("ETH", "EVH", maxSeatsEVR);
                numToCallWTHA = updateNumPatients("WTHA", "MDWA", maxSeatsMDW);
                numToCallWTHC = updateNumPatients("WTHC", "MDWC", maxSeatsMDW);
                numToCallMDWA = updateNumPatients("MDWA", "MDRA", maxSeatsMDR);
                numToCallMDWC = updateNumPatients("MDWC", "MDRC", maxSeatsMDR);

            } finally {
                lock1.unlock();
            }
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

    public int updateNumPatients(String hallToExit, String hallToEnter, int maxSeatsHallToEnter) {
        int numToCall = 0;
        int nToExit = hallPatients.get(hallToExit);
        int nToEnter = hallPatients.get(hallToEnter);


        int freeSpaces = maxSeatsHallToEnter - nToEnter;
        numToCall = freeSpaces <= nToExit ? freeSpaces : nToExit;
        nToExit -= numToCall;
        nToEnter += numToCall;

        if (!hallToEnter.equals("MDRA") && !hallToEnter.equals("MDRC"))
            hallPatients.put(hallToExit, nToExit);

        hallPatients.put(hallToEnter, nToEnter);

        return numToCall;
    }
}
