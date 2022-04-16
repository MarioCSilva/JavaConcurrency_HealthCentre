package HC.EvaluationHall;

import HC.Entities.TNurse;
import HC.Entities.TPatient;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MEvaluationHall implements IEvaluationHall_Patient, IEvaluationHall_Nurse {
    private final int nRooms = 4;

    private final ReentrantLock rl;

    private final TPatient rooms[];
    private final Condition cPatient[];
    private final boolean bPatient[];
    private final Condition cNurse[];
    private final boolean bNurse[];


    public MEvaluationHall() {
        this.rl = new ReentrantLock();
        this.rooms = new TPatient[nRooms];

        this.cNurse = new Condition[nRooms];
        this.cPatient = new Condition[nRooms];
        this.bPatient = new boolean[nRooms];
        this.bNurse = new boolean[nRooms];

        for (int i = 0; i < nRooms; i++) {
            this.cNurse[i] = this.rl.newCondition();
            this.cPatient[i] = this.rl.newCondition();
            this.bPatient[i] = false;
            this.bNurse[i] = false;
        }
    }

    public void enterHall(TPatient patient) throws InterruptedException, IOException {
        int chosenEVR = 0;

        patient.tSleep();

        try {
            rl.lock();

            for (int i = 0; i < nRooms; i++) {
                if (rooms[i] == null) {
                    chosenEVR = i;
                    break;
                }
            }

            patient.log(String.format("EVR%d", chosenEVR + 1));

            rooms[chosenEVR] = patient;

            bNurse[chosenEVR] = true;
            cNurse[chosenEVR].signal();

            while (!bPatient[chosenEVR])
                cPatient[chosenEVR].await();

            bPatient[chosenEVR] = false;

            patient.checkSuspend();

        } finally {
            rl.unlock();
        }

        patient.notifyExit("EVH");
    }


    public void work(TNurse nurse) throws InterruptedException, IOException {
        int roomId = nurse.getRoomId();
        TPatient patient = null;

        while (true) {
            try {
                rl.lock();

                while (!bNurse[roomId])
                    cNurse[roomId].await();

                bNurse[roomId] = false;

                patient = rooms[roomId];
            } finally {
                rl.unlock();
            }

            nurse.evaluate(patient);

            patient.log(String.format("EVR%d", roomId + 1));

            try {
                rl.lock();
                bPatient[roomId] = true;
                cPatient[roomId].signal();
                rooms[roomId] = null;
            } finally {
                rl.unlock();
            }

        }
    }
}
