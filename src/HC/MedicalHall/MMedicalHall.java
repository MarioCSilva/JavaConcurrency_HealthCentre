package HC.MedicalHall;

import HC.Entities.TDoctor;
import HC.Entities.TPatient;
import HC.Queue.PriorityQueue;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MMedicalHall implements IMedicalHall_CallCentre, IMedicalHall_Doctor, IMedicalHall_Patient {
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

    private final PriorityQueue MDRA;
    private final Condition cArrayMDRA[];
    private final boolean[] bExitMDRA;
    private final Condition cNotFullMDRA;
    private final Condition cNotEmptyMDRA;

    private final PriorityQueue MDRC;
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

        for (int i = 0; i < nRooms; i++) {
            this.cDoctor[i] = this.rl.newCondition();
            this.cPatient[i] = this.rl.newCondition();
            this.bPatient[i] = false;
            this.bDoctor[i] = false;
        }

        this.MDRA = new PriorityQueue(size);
        this.cArrayMDRA = new Condition[size];
        this.cNotEmptyMDRA = rl.newCondition();
        this.cNotFullMDRA = rl.newCondition();
        this.bExitMDRA = new boolean[size];

        this.MDRC = new PriorityQueue(size);
        this.cArrayMDRC = new Condition[size];
        this.cNotEmptyMDRC = rl.newCondition();
        this.cNotFullMDRC = rl.newCondition();
        this.bExitMDRC = new boolean[size];

        for (int i = 0; i < size; i++) {
            this.bExitMDRA[i] = false;
            this.cArrayMDRA[i] = rl.newCondition();
            this.bExitMDRC[i] = false;
            this.cArrayMDRC[i] = rl.newCondition();
        }
    }

    public void enterHall(TPatient patient) throws InterruptedException, IOException {
        PriorityQueue patientRoom = null;
        Condition cNotFull = null;
        int patientIdx = 0;
        Condition patientWaiting = null;

        if (patient.getIsAdult()) {
            patientWaiting = cAdultWaiting;
            patientRoom = MDRA;
            cNotFull = cNotFullMDRA;
            patientIdx = 2;
        } else {
            patientWaiting = cChildWaiting;
            patientRoom = MDRC;
            cNotFull = cNotFullMDRC;
            patientIdx = 0;
        }

        // walk to waiting room
        patient.tSleep();

        try {
            rlW.lock();

            patient.log("MDH");

            // wait for call centre to call him to an MDR
            if (patient.getIsAdult()) {
                while (!bAdultWaiting)
                    patientWaiting.await();
                bAdultWaiting = false;
            } else {
                while (!bChildWaiting)
                    patientWaiting.await();
                bChildWaiting = false;
            }

            patient.checkSuspend();

        } finally {
            rlW.unlock();
        }

        patient.notifyExit("MDW");

        try {
            rl.lock();

            // wait while room is full
            while (patientRoom.isFull())
                cNotFull.await();

            // assign the patient to a room
            patientIdx += patientRoom.put(patient);

            // increase room counter
            patientRoom.incCounter();

            patient.log(String.format("MDR%d", patientIdx + 1));

        } finally {
            rl.unlock();
        }

        // walk to corresponding medical room
        patient.tSleep();

        try {
            rl.lock();

            bDoctor[patientIdx] = true;
            cDoctor[patientIdx].signal();

            while (!bPatient[patientIdx])
                cPatient[patientIdx].await();

            bPatient[patientIdx] = false;

            // patientRoom.getPatientById( patientIdx % size );

            // if ( patientRoom.isFull() )
            //     cNotFull.signal();

            // patientRoom.decCounter();

        } finally {
            rl.unlock();
        }

        patient.notifyExit("MDR");
    }


    public void work(TDoctor doctor) throws InterruptedException {
        int roomId = doctor.getRoomId();
        TPatient patient = null;
        PriorityQueue patientRoom;
        int patientIdx;
        Condition cNotFull;

        if (roomId > 1) {
            patientRoom = MDRA;
            patientIdx = roomId % size;
            cNotFull = cNotFullMDRA;
        } else {
            patientRoom = MDRC;
            patientIdx = roomId;
            cNotFull = cNotFullMDRC;
        }

        while (true) {
            try {
                rl.lock();

                while (!bDoctor[roomId])
                    cDoctor[roomId].await();

                bDoctor[roomId] = false;

                patient = patientRoom.getQueue()[patientIdx];

                patient.checkSuspend();

            } finally {
                rl.unlock();
            }

            doctor.evaluate(patient);

            try {
                rl.lock();
                bPatient[roomId] = true;
                cPatient[roomId].signal();

                patientRoom.getPatientById(patientIdx);

                if (patientRoom.isFull())
                    cNotFull.signal();

                patientRoom.decCounter();

            } finally {
                rl.unlock();
            }
        }
    }

    public void exitWaitingRoom(String patientType) {
        rlW.lock();

        if (patientType.equals("A")) {
            bAdultWaiting = true;
            cAdultWaiting.signal();
        } else {
            bChildWaiting = true;
            cChildWaiting.signal();
        }

        rlW.unlock();
    }
}
