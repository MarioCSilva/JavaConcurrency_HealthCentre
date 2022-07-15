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

    private final PriorityQueue ETR2;
    private final Condition cArrayETR2[];
    private final boolean[] bExitETR2;
    private final Condition cNotFullETR2;


    public MEntranceHall(int nos) {
        this.size = nos / 2;
        this.rl = new ReentrantLock();

        this.ETR1 = new PriorityQueue(size);
        this.cArrayETR1 = new Condition[size];
        this.cNotFullETR1 = rl.newCondition();
        this.bExitETR1 = new boolean[size];

        this.ETR2 = new PriorityQueue(size);
        this.cArrayETR2 = new Condition[size];
        this.cNotFullETR2 = rl.newCondition();
        this.bExitETR2 = new boolean[size];

        for (int i = 0; i < size; i++) {
            this.bExitETR1[i] = false;
            cArrayETR1[i] = rl.newCondition();
            this.bExitETR2[i] = false;
            cArrayETR2[i] = rl.newCondition();
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
        // get 
        PriorityQueue patientRoom = null;
        String room = null;
        Condition cArray[] = null;
        boolean bExit[] = null;
        Condition cNotFull = null;
        int patientIdx = 0;

        if (patient.getIsAdult()) {
            room = "ET2";
            patientRoom = ETR2;
            cArray = cArrayETR2;
            cNotFull = cNotFullETR2;
            bExit = bExitETR2;
        } else {
            room = "ET1";
            patientRoom = ETR1;
            cArray = cArrayETR1;
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

            // wait while room is full or when the patient is not the next one in line for his associated queue
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

        // walk to room
        patient.tSleep();

        // notify Call Centre of entrance in the room of ETH
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

    
    /** 
     * Get the patient with the highest priority from the queue @ETR.
     * The priority is defined by the @ETN variable.
     * @param ETR A priority Queue where the Patient is.
     * @param maxPriorityPatient aTuple with the Patient, and the index where Patient is in the priority queue
     * @return List<Object> a Tuple with the Patient, and the index where Patient is in the priority queue
     */
    public List<Object> getHighestPriorityPatient(PriorityQueue ETR, List<Object> maxPriorityPatient) {
        for (int i = 0; i < size; i++) {
            TPatient patient = ETR.getQueue()[i];
            TPatient priorityPatient = (TPatient) maxPriorityPatient.get(0);
            if (patient != null && (priorityPatient == null || (patient.getTN() < priorityPatient.getTN())))
                maxPriorityPatient = Arrays.asList(patient, i);
        }
        return maxPriorityPatient;
    }



    /** 
     * Method to be called by the Call Centre Entity to allow a Patient with the highest priority to leave this hall.
     *
     */
    public void exitHall() {
        List<Object> maxPriorityPatient = Arrays.asList(null, 0);

        rl.lock();

        maxPriorityPatient = getHighestPriorityPatient(ETR1, maxPriorityPatient);
        maxPriorityPatient = getHighestPriorityPatient(ETR2, maxPriorityPatient);

        if (maxPriorityPatient.get(0) != null)
            exitHall((TPatient) maxPriorityPatient.get(0), (int) maxPriorityPatient.get(1));

        rl.unlock();
    }

    
    /** 
     * @param patient a Patient that will leave this hall
     * @param idx the index where Patient is in the priority queue
     */
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

        // wake up pacient to make him leave the hall
        bExit[idx] = true;
        cArray[idx].signal();

        // remove patient from queue
        patientRoom.getPatientById(idx);

        // signall all patients that were waiting to get a room assigned
        cNotFull.signalAll();

        patientRoom.decCounter();
    }
}
