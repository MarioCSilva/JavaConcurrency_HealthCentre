package HC.WaitingHall;

import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class MWaitingHall implements IWaitingHall_CallCentre, IWaitingHall_Patient {
    private int WTN = 0;
    private final int size;

    private final ReentrantLock rl;
    private final MFIFO WTR1;
    private final Condition cArrayWTR1[];
    private final boolean[] bExitWTR1;
    private final Condition cNotFullWTR1;
    private final Condition cNotEmptyWTR1;

    private final MFIFO WTR2;
    private final Condition cArrayWTR2[];
    private final boolean[] bExitWTR2;
    private final Condition cNotFullWTR2;
    private final Condition cNotEmptyWTR2;


    public MWaitingHall(int nos) {
        this.size = nos/2;
        this.rl = new ReentrantLock();

        this.WTR1 = new MFIFO(size);
        this.cArrayWTR1 = new Condition[size];
        this.cNotEmptyWTR1 = rl.newCondition();
        this.cNotFullWTR1 = rl.newCondition();
        this.bExitWTR1 = new boolean[size];

        this.WTR2 = new MFIFO(size);
        this.cArrayWTR2 = new Condition[size];
        this.cNotEmptyWTR2 = rl.newCondition();
        this.cNotFullWTR2 = rl.newCondition();
        this.bExitWTR2 = new boolean[size];

        for(int i = 0; i< size; i++){
            this.bExitWTR1[i] = false;
            cArrayWTR1[i] = rl.newCondition();
            this.bExitWTR2[i] = false;
            cArrayWTR2[i] = rl.newCondition();
        }
    }

    public void enterHall(TPatient patient){
        MFIFO patientRoom = null;
        String room = null;
        Condition cArray[] = null;
        boolean bExit[] = null;
        Condition cNotEmpty = null;
        Condition cNotFull = null;

        if (patient.getIsAdult()) {
            room = "WTR2";
            patientRoom = WTR2;
            cArray = cArrayWTR2;
            cNotEmpty = cNotEmptyWTR2;
            cNotFull = cNotFullWTR2;
            bExit = bExitWTR2;
        } else {
            room = "WTR1";
            patientRoom = WTR1;
            cArray = cArrayWTR1;
            cNotEmpty = cNotEmptyWTR1;
            cNotFull = cNotFullWTR1;
            bExit = bExitWTR1;
        }

        rl.lock();

        patient.setTN(WTN);
        WTN++;

        patient.log("WTH");

        rl.unlock();

        patient.tSleep();

        try {
            rl.lock();

            // wait while fifo is full
            while (patientRoom.isFull())
                cNotFull.await();   

            // assign the patient to a room
            int patientIdx = patientRoom.put(patient);

            // check if fifo was empty and send a signal if it was
            if ( patientRoom.isEmpty() )
                cNotEmpty.signal();

            // increase fifo counter
            patientRoom.incCounter();

            patient.log(room);

            patient.notifyEntrance("WTH");

            // stay blocked on fifo since it has entered
            while ( !bExit[ patientIdx ] )
                cArray[ patientIdx ].await();

            bExit[ patientIdx ] = false;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rl.unlock();
        }
    }

    public List<Object> getHighestPriorityPatient(MFIFO WTR, List<Object> maxPriorityPatient) {
        for (int i=0; i<size; i++) {
            TPatient patient = WTR.getFIFO()[i];
            TPatient priorityPatient = (TPatient) maxPriorityPatient.get(0);
            if ( patient != null && ( priorityPatient == null || (patient.getDoS().compareTo(priorityPatient.getDoS()) > 0) ||
                (patient.getDoS().compareTo(priorityPatient.getDoS()) == 0 && patient.getTN() < priorityPatient.getTN()) ) )
                maxPriorityPatient = Arrays.asList(patient, i);
        }
        return maxPriorityPatient;
    }

    public void exitHall() {
        List<Object> maxPriorityPatient = Arrays.asList(null, 0);

        rl.lock();

        maxPriorityPatient = getHighestPriorityPatient(this.WTR1, maxPriorityPatient);
        maxPriorityPatient = getHighestPriorityPatient(this.WTR2, maxPriorityPatient);
        System.out.println(maxPriorityPatient);

        if (maxPriorityPatient.get(0) != null)
            exitHall((TPatient) maxPriorityPatient.get(0), (int) maxPriorityPatient.get(1));

        rl.unlock();
    }

    public void exitHall(TPatient patient, int idx) {
        MFIFO patientWTR = null;
        Condition cArray[] = null;
        boolean bExit[] = null;
        Condition cNotFull = null;

        if (patient.getIsAdult()) {
            patientWTR = WTR2;
            cArray = cArrayWTR2;
            cNotFull = cNotFullWTR2;
            bExit = bExitWTR2;
        } else {
            patientWTR = WTR1;
            cArray = cArrayWTR1;
            cNotFull = cNotFullWTR1;
            bExit = bExitWTR1;
        }

        if ( patientWTR.isFull() )
            cNotFull.signal();

        patientWTR.decCounter();

        patientWTR.getPatientById(idx);

        bExit[ idx ] = true;
        cArray[ idx ].signal();
    }
}
