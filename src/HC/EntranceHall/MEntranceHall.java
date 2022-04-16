package HC.EntranceHall;


import HC.Entities.TPatient;
import HC.Queue.PriorityQueue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MEntranceHall implements IEntranceHall_CallCentre, IEntranceHall_Patient {
    private int ETN = 1;
    private int ETN1 = 1;
    private int ETN2 = 1;
    private int currentETN1 = 1;
    private int currentETN2 = 1;

    private final int size;

    private final ReentrantLock rl;

    private final PriorityQueue ETR1;
    private final Condition cArrayETR1[];
    private final boolean[] bExitETR1;
    private final Condition cNotFullETR1;
    private final Condition cNotEmptyETR1;

    private final PriorityQueue ETR2;
    private final Condition cArrayETR2[];
    private final boolean[] bExitETR2;
    private final Condition cNotFullETR2;
    private final Condition cNotEmptyETR2;


    public MEntranceHall(int nos) {
        this.size = nos / 2;
        this.rl = new ReentrantLock();

        this.ETR1 = new PriorityQueue(size);
        this.cArrayETR1 = new Condition[size];
        this.cNotEmptyETR1 = rl.newCondition();
        this.cNotFullETR1 = rl.newCondition();
        this.bExitETR1 = new boolean[size];

        this.ETR2 = new PriorityQueue(size);
        this.cArrayETR2 = new Condition[size];
        this.cNotEmptyETR2 = rl.newCondition();
        this.cNotFullETR2 = rl.newCondition();
        this.bExitETR2 = new boolean[size];

        for (int i = 0; i < size; i++) {
            this.bExitETR1[i] = false;
            cArrayETR1[i] = rl.newCondition();
            this.bExitETR2[i] = false;
            cArrayETR2[i] = rl.newCondition();
        }

    }

    public void enterHall(TPatient patient) throws InterruptedException, IOException {
        PriorityQueue patientRoom = null;
        String room = null;
        Condition cArray[] = null;
        boolean bExit[] = null;
        Condition cNotEmpty = null;
        Condition cNotFull = null;
        int patientIdx = 0;

        if (patient.getIsAdult()) {
            room = "ET2";
            patientRoom = ETR2;
            cArray = cArrayETR2;
            cNotEmpty = cNotEmptyETR2;
            cNotFull = cNotFullETR2;
            bExit = bExitETR2;
        } else {
            room = "ET1";
            patientRoom = ETR1;
            cArray = cArrayETR1;
            cNotEmpty = cNotEmptyETR1;
            cNotFull = cNotFullETR1;
            bExit = bExitETR1;
        }


        try {
            rl.lock();

            int patientETN;

            // give an ETN to each patient according to their age(Child or Adult) upon entering ETH
            if (patient.getIsAdult()) {
                patient.setTN(ETN++);
                patientETN = ETN2++;
            } else {
                patient.setTN(ETN++);
                patientETN = ETN1++;
            }

            patient.log("ETH");

            // wait while room is full
            while (patientRoom.isFull() || (patient.getIsAdult() && currentETN2 != patientETN) ||
                    (!patient.getIsAdult() && currentETN1 != patientETN))
                cNotFull.await();

            patient.checkSuspend();

            // assign the patient to a room
            patientIdx = patientRoom.put(patient);

            // increase room counter
            patientRoom.incCounter();

            patient.log(room);

        } finally {
            rl.unlock();
        }

        patient.tSleep();

        patient.notifyEntrance("ETH");

        try {
            rl.lock();

            // stay blocked on room since it has entered
            while (!bExit[patientIdx])
                cArray[patientIdx].await();

            bExit[patientIdx] = false;

            patient.checkSuspend();

        } finally {
            rl.unlock();
        }
    }

    public List<Object> getHighestPriorityPatient(PriorityQueue ETR, List<Object> maxPriorityPatient) {
        for (int i = 0; i < size; i++) {
            TPatient patient = ETR.getQueue()[i];
            TPatient priorityPatient = (TPatient) maxPriorityPatient.get(0);
            if (patient != null && (priorityPatient == null || (patient.getTN() < priorityPatient.getTN())))
                maxPriorityPatient = Arrays.asList(patient, i);
        }
        return maxPriorityPatient;
    }

    public void exitHall() {
        List<Object> maxPriorityPatient = Arrays.asList(null, 0);

        rl.lock();

        maxPriorityPatient = getHighestPriorityPatient(ETR1, maxPriorityPatient);
        maxPriorityPatient = getHighestPriorityPatient(ETR2, maxPriorityPatient);

        if (maxPriorityPatient.get(0) != null)
            exitHall((TPatient) maxPriorityPatient.get(0), (int) maxPriorityPatient.get(1));

        rl.unlock();
    }

    public void exitHall(TPatient patient, int idx) {
        Condition cArray[];
        PriorityQueue patientRoom;
        Condition cNotFull;
        boolean bExit[];

        if (patient.getIsAdult()) {
            currentETN2++;
            patientRoom = ETR2;
            cArray = cArrayETR2;
            bExit = bExitETR2;
            cNotFull = cNotFullETR2;
        } else {
            currentETN1++;
            patientRoom = ETR1;
            cArray = cArrayETR1;
            bExit = bExitETR1;
            cNotFull = cNotFullETR1;
        }

        bExit[idx] = true;
        cArray[idx].signal();

        patientRoom.getPatientById(idx);

        cNotFull.signalAll();

        patientRoom.decCounter();
    }
}
