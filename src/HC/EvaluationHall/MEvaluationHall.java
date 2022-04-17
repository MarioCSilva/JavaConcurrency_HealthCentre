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

    
    /** 
     * Method to be called by a Patient Entity to enter this hall.
     *
     * @param patient a Patient that has entered a Hall
     * @throws InterruptedException
     * @throws IOException
     */
    public void enterHall(TPatient patient) throws InterruptedException, IOException {
        int chosenEVR = 0;
        // sleep between halls/rooms
        patient.tSleep();

        try {
            rl.lock();
            // chooses an empty EVR for the Patient
            for (int i = 0; i < nRooms; i++) {
                if (rooms[i] == null) {
                    chosenEVR = i;
                    break;
                }
            }
            //write the room where the patient has been assigned to the log file
            patient.log(String.format("EVR%d", chosenEVR + 1));

            rooms[chosenEVR] = patient;
            //wake up the nurse where the Patient is
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


    
    /** 
     * Method called by the Nurse check for patients in its room and evaluate them.
     *
     * @param nurse the Nurse of the room
     * @throws InterruptedException
     * @throws IOException
     */
    public void work(TNurse nurse) throws InterruptedException, IOException {
        //get the Index of the Room where the nurse is
        int roomId = nurse.getRoomId();
        TPatient patient = null;

        while (true) {
            try {
                rl.lock();

                //check if its the respective nurse's room if not, awaits
                while (!bNurse[roomId])
                    cNurse[roomId].await();

                bNurse[roomId] = false;
                // get the patient in the room
                patient = rooms[roomId];
            } finally {
                rl.unlock();
            }
            //evaluate the patient
            nurse.evaluate(patient);

            patient.log(String.format("EVR%d", roomId + 1));

            try {
                rl.lock();
                bPatient[roomId] = true;
                // signal the patient to carry on its lifecycle
                cPatient[roomId].signal();
                rooms[roomId] = null;
            } finally {
                rl.unlock();
            }

        }
    }
}
