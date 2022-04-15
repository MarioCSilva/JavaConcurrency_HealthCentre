package HC.MedicalHall;

import HC.Entities.TDoctor;
import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MMedicalHall implements IMedicalHall_CallCentre, IMedicalHall_Doctor, IMedicalHall_Patient {
    // representing the waiting room, one seat for adult and another for a child
    private TPatient childWaiting;
    private TPatient adultWaiting;

    private final int nRooms = 4;
    private final int size = 2;
    
    private final ReentrantLock rlW;

    private final Condition cAdultWaiting;
    private boolean bAdultWaiting;

    private final Condition cChildWaiting;
    private boolean bChildWaiting;

    private final ReentrantLock rl;

    private final Condition cPatient[];
    private final boolean bPatient[];
    private final Condition cDoctor[];
    private final boolean bDoctor[];

    private final MFIFO MDRA;
    private final Condition cArrayMDRA[];
    private final boolean[] bExitMDRA;
    private final Condition cNotFullMDRA;
    private final Condition cNotEmptyMDRA;

    private final MFIFO MDRC;
    private final Condition cArrayMDRC[];
    private final boolean[] bExitMDRC;
    private final Condition cNotFullMDRC;
    private final Condition cNotEmptyMDRC;

    public MMedicalHall() {
        this.rlW = new ReentrantLock();
        this.cChildWaiting = this.rlW.newCondition();
        this.cAdultWaiting = this.rlW.newCondition();
        this.bChildWaiting = false;
        this.bAdultWaiting = false;


        this.rl = new ReentrantLock();

        this.cDoctor = new Condition[nRooms];
        this.cPatient = new Condition[nRooms];
        this.bPatient = new boolean[nRooms];
        this.bDoctor = new boolean[nRooms];

        for (int i=0; i<nRooms; i++){
            this.cDoctor[i] = this.rl.newCondition();
            this.cPatient[i] = this.rl.newCondition();
            this.bPatient[i] = false;
            this.bDoctor[i] = false;
        }

        this.MDRA = new MFIFO(size);
        this.cArrayMDRA = new Condition[size];
        this.cNotEmptyMDRA = rl.newCondition();
        this.cNotFullMDRA = rl.newCondition();
        this.bExitMDRA = new boolean[size];

        this.MDRC = new MFIFO(size);
        this.cArrayMDRC = new Condition[size];
        this.cNotEmptyMDRC = rl.newCondition();
        this.cNotFullMDRC = rl.newCondition();
        this.bExitMDRC = new boolean[size];

        for(int i = 0; i< size; i++) {
            this.bExitMDRA[i] = false;
            this.cArrayMDRA[i] = rl.newCondition();
            this.bExitMDRC[i] = false;
            this.cArrayMDRC[i] = rl.newCondition();
        }
    }

    public void enterHall(TPatient patient) {
        MFIFO patientRoom = null;
        Condition cNotFull = null;
        int patientIdx = 0;
        Condition patientWaiting = null;
        boolean bPatientWaiting = false;

        if (patient.getIsAdult()) {
            patientWaiting = cAdultWaiting;
            bPatientWaiting = bAdultWaiting;

            patientRoom = MDRA;
            cNotFull = cNotFullMDRA;
            patientIdx = 0;
        } else {
            patientWaiting = cChildWaiting;
            bPatientWaiting = bChildWaiting;

            patientRoom = MDRC;
            cNotFull = cNotFullMDRC;
            patientIdx = 2;
        }
        
        patient.log("MDH");

        patient.notifyExit("MDW");

        try {
            rlW.lock();

            // wait for call centre to call him to an MDR
            while (bPatientWaiting)
                patientWaiting.await();

            if (patient.getIsAdult())
                bAdultWaiting = true;
            else
                bChildWaiting = true;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rlW.unlock();
        }

        patient.notifyExit("MDW");

        patient.tSleep();

        try {
            rl.lock();

            // wait while room is full
            while (patientRoom.isFull())
                cNotFull.await();

            // assign the patient to a room
            patientIdx += patientRoom.put(patient);

            // increase room counter
            patientRoom.incCounter();

            patient.log(String.format("MDR%d", patientIdx));

        } catch (InterruptedException e) {
                e.printStackTrace();
        } finally {
            rl.unlock();
        }

        patient.tSleep();

        try {
            rl.lock();

            bDoctor[patientIdx] = true;
            cDoctor[patientIdx].signal();

            while ( !bPatient[patientIdx] )
                cPatient[patientIdx].await();

            bPatient[patientIdx] = false;

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

        patient.notifyExit("MDR");
    }


    public void work(TDoctor doctor) {
        int roomId = doctor.getRoomId();
        TPatient patient = null;

        while (true) {
            try {
                rl.lock();

                bDoctor[roomId] = false;
                while ( !bDoctor[roomId] )
                    cDoctor[roomId].await();
                                
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                rl.unlock();
            }

            doctor.evaluate( patient );
            patient.log(String.format("MDR%d", roomId+1));

            try {
                rl.lock();
                bPatient[roomId] = true;
                cPatient[roomId].signal();
            } finally {
                rl.unlock();
            }
        }
    }

    public void exitHall(String patientType) {
        rlW.lock();

        if (patientType.equals("A")) {
            cAdultWaiting.signal();
            bAdultWaiting = true;
        } else {
            cChildWaiting.signal();
            bChildWaiting = true;
        }

        rlW.unlock();
    }
}
