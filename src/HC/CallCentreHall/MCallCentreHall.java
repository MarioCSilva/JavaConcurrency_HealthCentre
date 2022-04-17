package HC.CallCentreHall;

import HC.Controller.IController_CallCentreHall;
import HC.Entities.TCallCentre;
import HC.Entities.TPatient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MCallCentreHall implements ICallCentreHall_Patient,
        ICallCentreHall_CallCentre {
    private final ReentrantLock lock1;

    private final IController_CallCentreHall controller;
    private Condition cAwakeCC;

    private final HashMap<String, Integer> hallPatients;

    public MCallCentreHall(int nos, IController_CallCentreHall controller) {
        this.hallPatients = new HashMap<>();
        this.hallPatients.put("ETH", 0);
        this.hallPatients.put("EVH", 0);
        this.hallPatients.put("WTHA", 0);
        this.hallPatients.put("WTHC", 0);
        this.hallPatients.put("MDWA", 0);
        this.hallPatients.put("MDWC", 0);
        this.hallPatients.put("MDRA", 0);
        this.hallPatients.put("MDRC", 0);

        this.controller = controller;
        this.lock1 = controller.getCCHLock();
        this.cAwakeCC = controller.getCAwakeCC();
    }


    public void notifyEntrance(TPatient patient, String hall) {
        lock1.lock();

        if (hall.equals("WTH"))
            hall += patient.getPatientType();

        int patients = hallPatients.get(hall);
        hallPatients.put(hall, patients + 1);

        this.cAwakeCC.signal();
        this.controller.setBAwakeCC(true);

        lock1.unlock();
    }

    public void notifyExit(TPatient patient, String hall) {
        lock1.lock();

        if (hall.equals("MDW") || hall.equals("MDR"))
            hall += patient.getPatientType();

        System.out.println(patient);
        System.out.println(Arrays.toString(this.hallPatients.entrySet().toArray()));


        int patients = hallPatients.get(hall);
        hallPatients.put(hall, patients - 1);
        System.out.println("a sair");
        System.out.println(Arrays.toString(this.hallPatients.entrySet().toArray()));

        this.cAwakeCC.signal();
        this.controller.setBAwakeCC(true);

        lock1.unlock();
    }

    public void work(TCallCentre cc) throws InterruptedException {
        int numToCallETH = 0, numToCallWTHA = 0, numToCallWTHC = 0, numToCallMDWA = 0, numToCallMDWC = 0, i = 0;
        boolean isManualMode = false;
        int maxSeatsEVR = 4;
        int maxSeatsMDW = 1;
        int maxSeatsMDR = 2;

        while (true) {

            try {
                lock1.lock();

                System.out.println("dormir");

                while (!this.controller.getBAwakeCC())
                    this.cAwakeCC.await();

                System.out.println("acordar");

                this.controller.setBAwakeCC(false);

                isManualMode = cc.getController().checkManualMode();

                int totalCalledPatients = 0;

                numToCallETH = updateNumPatients("ETH", "EVH", maxSeatsEVR,
                        totalCalledPatients, isManualMode);
                totalCalledPatients += numToCallETH;

                numToCallWTHC = updateNumPatients("WTHC", "MDWC", maxSeatsMDW,
                        totalCalledPatients, isManualMode);
                System.out.println(String.format("WTHC to MDWC %d", numToCallWTHC));
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

    public int updateNumPatients(String hallToExit, String hallToEnter, int maxSeatsHallToEnter,
                                 int totalCalledPatients, boolean isManualMode) {
        int numToCall = 0;
        int nToExit = hallPatients.get(hallToExit);
        int nToEnter = hallPatients.get(hallToEnter);

        int freeSpaces = maxSeatsHallToEnter - nToEnter;
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

        if (!hallToEnter.equals("MDRA") && !hallToEnter.equals("MDRC"))
            hallPatients.put(hallToExit, nToExit);

        hallPatients.put(hallToEnter, nToEnter);

        return numToCall;
    }
}
