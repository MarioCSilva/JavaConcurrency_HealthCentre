package HC.WaitingHall;

import HC.Entities.TPatient;
import HC.Queue.PriorityQueue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class MWaitingHall implements IWaitingHall_CallCentre, IWaitingHall_Patient {
    private int WTN = 1;
    private int WTN1 = 1;
    private int WTN2 = 1;
    private int currentWTN1 = 1;
    private int currentWTN2 = 1;

    private final int size;

    private final ReentrantLock rl;
    private final PriorityQueue WTR1;
    private final Condition cArrayWTR1[];
    private final boolean[] bExitWTR1;
    private final Condition cNotFullWTR1;

    private final PriorityQueue WTR2;
    private final Condition cArrayWTR2[];
    private final boolean[] bExitWTR2;
    private final Condition cNotFullWTR2;


    public MWaitingHall(int nos) {
        this.size = nos / 2;

        this.rl = new ReentrantLock();

        this.WTR1 = new PriorityQueue(size);
        this.cArrayWTR1 = new Condition[size];
        this.cNotFullWTR1 = rl.newCondition();
        this.bExitWTR1 = new boolean[size];

        this.WTR2 = new PriorityQueue(size);
        this.cArrayWTR2 = new Condition[size];
        this.cNotFullWTR2 = rl.newCondition();
        this.bExitWTR2 = new boolean[size];

        for (int i = 0; i < size; i++) {
            this.bExitWTR1[i] = false;
            cArrayWTR1[i] = rl.newCondition();
            this.bExitWTR2[i] = false;
            cArrayWTR2[i] = rl.newCondition();
        }
    }

    
    /** 
     * Method to be called by a Patient Entity to enter this hall.
     *
     * @param patient a Patient that has entered a Hall
     * @throws InterruptedException
     * @throws IOException
    */
    public void enterHall(TPatient patient) throws InterruptedException, IOException {
        PriorityQueue patientRoom = null;
        String room = null;
        Condition cArray[] = null;
        boolean bExit[] = null;
        Condition cNotFull = null;
        int patientIdx = 0;

        if (patient.getIsAdult()) {
            room = "WTR2";
            patientRoom = WTR2;
            cArray = cArrayWTR2;
            cNotFull = cNotFullWTR2;
            bExit = bExitWTR2;
        } else {
            room = "WTR1";
            patientRoom = WTR1;
            cArray = cArrayWTR1;
            cNotFull = cNotFullWTR1;
            bExit = bExitWTR1;
        }
        try {
            rl.lock();

            int patientWTN;

            if (patient.getIsAdult()) {
                patient.setTN(WTN++);
                patientWTN = WTN2++;
            } else {
                patient.setTN(WTN++);
                patientWTN = WTN1++;
            }

            patient.log("WTH");

            // wait while room is full
            while (patientRoom.isFull() || (patient.getIsAdult() && currentWTN2 != patientWTN) ||
                    (!patient.getIsAdult() && currentWTN1 != patientWTN))
                cNotFull.await();

            patient.checkSuspend();

            // assign the patient to a room
            patientIdx = patientRoom.put(patient);

            // increase queue counter
            patientRoom.incCounter();

            patient.log(room);

        } finally {
            rl.unlock();
        }

        patient.tSleep();

        patient.notifyEntrance("WTH");

        try {
            rl.lock();

            // stay blocked on queue since it has entered
            while (!bExit[patientIdx])
                cArray[patientIdx].await();

            bExit[patientIdx] = false;

            patient.checkSuspend();

        } finally {
            rl.unlock();
        }
    }

    
    /** 
     * Get the patient with the highest priority from the queue @ETR.
     * The priority is defined by the Patients DoS and @ETN variable.
     * @param WTR A priority Queue where the Patient is.
     * @param maxPriorityPatient aTuple with the Patient, and the index where Patient is in the priority queue
     * @return List<Object> a Tuple with the Patient, and the index where Patient is in the priority queue
     */
    public List<Object> getHighestPriorityPatient(PriorityQueue WTR, List<Object> maxPriorityPatient) {
        for (int i = 0; i < size; i++) {
            TPatient patient = WTR.getQueue()[i];
            TPatient priorityPatient = (TPatient) maxPriorityPatient.get(0);
            if (patient != null && (priorityPatient == null || (patient.getDoS().compareTo(priorityPatient.getDoS()) > 0) ||
                    (patient.getDoS().compareTo(priorityPatient.getDoS()) == 0 && patient.getTN() < priorityPatient.getTN())))
                maxPriorityPatient = Arrays.asList(patient, i);
        }
        return maxPriorityPatient;
    }

    
    /** 
     * @param patientType the Type of the Patient: A for Adults and C for Children
     */
    public void exitHall(String patientType) {
        List<Object> maxPriorityPatient = Arrays.asList(null, 0);
        Condition cArray[];
        boolean bExit[];
        Condition cNotFull;
        PriorityQueue patientRoom;

        rl.lock();

        if (patientType.equals("A")) {
            currentWTN2++;
            patientRoom = WTR2;
            maxPriorityPatient = getHighestPriorityPatient(this.WTR2, maxPriorityPatient);
            cArray = cArrayWTR2;
            bExit = bExitWTR2;
            cNotFull = cNotFullWTR2;
        } else {
            currentWTN1++;
            maxPriorityPatient = getHighestPriorityPatient(this.WTR1, maxPriorityPatient);
            cArray = cArrayWTR1;
            bExit = bExitWTR1;
            patientRoom = WTR1;
            cNotFull = cNotFullWTR1;
        }

        int idx = (int) maxPriorityPatient.get(1);

        if (maxPriorityPatient.get(0) != null) {
            bExit[idx] = true;
            cArray[idx].signal();

            patientRoom.getPatientById(idx);

            cNotFull.signalAll();

            patientRoom.decCounter();
        }

        rl.unlock();
    }
}
