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
            System.out.println("Apanhei o lock antes 1");

            rl.lock();
    
            if (patient.getIsAdult())
                patient.setTN(ETN2++);
            else
                patient.setTN(ETN1++);
    
            patient.log("ETH");
    
            // wait while room is full
            while (patientRoom.isFull())
                cNotFull.await();
    
            // assign the patient to a room
            patientRoom.put(patient, patient.getTN() % size);

            // check if room was empty and send a signal if it was
            // if ( patientRoom.isEmpty() )
            //     cNotEmpty.signal();

            // increase room counter
            patientRoom.incCounter();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("libertei 1");
            rl.unlock();
        }

        patient.tSleep();
        
        patient.notifyEntrance("ETH");

        try {
            System.out.println("Apanhei o lock antes 2");
            rl.lock();

            patient.log(room);
            int idx = patient.getTN() % size;
            // stay blocked on room since it has entered
            while ( !bExit[ idx ] )
                cArray[ idx ].await();

            bExit[ idx ] = false;

            patientRoom.getPatientById(idx);

            if ( patientRoom.isFull() )
                cNotFull.signal();

            patientRoom.decCounter();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("libertei 2");
            rl.unlock();
        }
    }

    public TPatient getHighestPriorityPatient(MFIFO ETR, TPatient maxPriorityPatient) {
        for (int i=0; i<size; i++) {
            TPatient patient = ETR.getFIFO()[i];
            if ( patient != null && ( maxPriorityPatient == null || (patient.getTN() < maxPriorityPatient.getTN()) ) )
                maxPriorityPatient = patient;
        }
        return maxPriorityPatient;
    }

    public void exitHall() {
        TPatient maxPriorityPatient = null;
        System.out.println(maxPriorityPatient);

        rl.lock();

        for (int i=0; i<size; i++) {
            System.out.println(String.format("etr 1 - %s", ETR1.getFIFO()[i]));
            System.out.println(String.format("etr 2 - %s", ETR2.getFIFO()[i]));
        }
        
        maxPriorityPatient = getHighestPriorityPatient(ETR1, maxPriorityPatient);
        maxPriorityPatient = getHighestPriorityPatient(ETR2, maxPriorityPatient);

        System.out.println(maxPriorityPatient);

        if (maxPriorityPatient != null)
            exitHall(maxPriorityPatient);

        rl.unlock();
    }

    public void exitHall(TPatient patient) {
        MFIFO patientRoom = null;
        Condition cArray[] = null;
        boolean bExit[] = null;
        Condition cNotFull = null;

        if (patient.getIsAdult()) {
            patientRoom = ETR2;
            cArray = cArrayETR2;
            cNotFull = cNotFullETR2;
            bExit = bExitETR2;
        } else {
            patientRoom = ETR1;
            cArray = cArrayETR1;
            cNotFull = cNotFullETR1;
            bExit = bExitETR1;
        }
        int idx = patient.getTN() % size;
        
        bExit[ idx ] = true;
        cArray[ idx ].signal();
    }
}