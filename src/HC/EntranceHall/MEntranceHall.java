package HC.EntranceHall;


import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MEntranceHall implements IEntranceHall_CallCentre, IEntranceHall_Patient {
    private int ETN1 = 1;
    private int ETN2 = 1;
    private final int size;

    private final ReentrantLock rl;

    private final MFIFO ETR1;
    private final Condition cArrayETR1[];
    private final boolean[] bExitETR1;
    private final Condition cNotFullETR1;
    private final Condition cNotEmptyETR1;

    private final MFIFO ETR2;
    private final Condition cArrayETR2[];
    private final boolean[] bExitETR2;
    private final Condition cNotFullETR2;
    private final Condition cNotEmptyETR2;
    

    public MEntranceHall(int nos) {
        this.size = nos/2;
        this.rl = new ReentrantLock();

        this.ETR1 = new MFIFO(size);
        this.cArrayETR1 = new Condition[size];
        this.cNotEmptyETR1 = rl.newCondition();
        this.cNotFullETR1 = rl.newCondition();
        this.bExitETR1 = new boolean[size];

        this.ETR2 = new MFIFO(size);
        this.cArrayETR2 = new Condition[size];
        this.cNotEmptyETR2 = rl.newCondition();
        this.cNotFullETR2 = rl.newCondition();
        this.bExitETR2 = new boolean[size];

        for(int i = 0; i< size; i++){
            this.bExitETR1[i] = false;
            cArrayETR1[i] = rl.newCondition();
            this.bExitETR2[i] = false;
            cArrayETR2[i] = rl.newCondition();
        }

    }

    public void enterHall(TPatient patient) {
        MFIFO patientRoom = null;
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

        // give an ETN to each patient upon entering ETH

        try {
            rl.lock();

            System.out.println(String.format("Entrei no ETH========%s", patient));
    
            if (patient.getIsAdult())
                patient.setTN(ETN2++);
            else
                patient.setTN(ETN1++);
    
            patient.log("ETH");
    
            // wait while room is full
            while (patientRoom.isFull()) {
                System.out.println(String.format("preso no ETH========%s, com %d", patient, patientRoom.getCount()));
                cNotFull.await();
            }

            System.out.println(String.format("fui colocado no ETH========%s", patient));

            // assign the patient to a room
            patientIdx = patientRoom.put(patient);

            // increase room counter
            patientRoom.incCounter();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rl.unlock();
        }

        patient.tSleep();

        patient.log(room);

        patient.notifyEntrance("ETH");

        try {
            rl.lock();

            // stay blocked on room since it has entered
            while ( !bExit[ patientIdx ] )
                cArray[ patientIdx ].await();

            bExit[ patientIdx ] = false;

            patientRoom.getPatientById(patientIdx);

            if ( patientRoom.isFull() )
                cNotFull.signal();

            patientRoom.decCounter();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(String.format("SAI DO ETH AGORA %s", patient));
            rl.unlock();
        }
    }

    public List<Object> getHighestPriorityPatient(MFIFO ETR, List<Object> maxPriorityPatient) {
        for (int i=0; i<size; i++) {
            TPatient patient = ETR.getFIFO()[i];
            TPatient priorityPatient = (TPatient) maxPriorityPatient.get(0);
            if ( patient != null && ( priorityPatient == null || (patient.getTN() < priorityPatient.getTN()) ) )
                maxPriorityPatient = Arrays.asList(patient, i);
        }
        return maxPriorityPatient;
    }

    public void exitHall() {
        List<Object> maxPriorityPatient = Arrays.asList(null, 0);
        System.out.println(maxPriorityPatient);

        rl.lock();

        for (int i=0; i<size; i++) {
            System.out.println(String.format("etr 1 - %s", ETR1.getFIFO()[i]));
            System.out.println(String.format("etr 2 - %s", ETR2.getFIFO()[i]));
        }
        
        maxPriorityPatient = getHighestPriorityPatient(ETR1, maxPriorityPatient);
        maxPriorityPatient = getHighestPriorityPatient(ETR2, maxPriorityPatient);

        System.out.println(maxPriorityPatient);

        if (maxPriorityPatient.get(0) != null)
            exitHall((TPatient) maxPriorityPatient.get(0), (int) maxPriorityPatient.get(1));

        rl.unlock();
    }

    public void exitHall(TPatient patient, int idx) {
        Condition cArray[];
        boolean bExit[];

        if (patient.getIsAdult()) {
            cArray = cArrayETR2;
            bExit = bExitETR2;
        } else {
            cArray = cArrayETR1;
            bExit = bExitETR1;
        }

        bExit[ idx ] = true;
        cArray[ idx ].signal();
    }
}
