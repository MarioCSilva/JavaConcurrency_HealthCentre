package HC.EvaluationHall;

import HC.Entities.TNurse;
import HC.Entities.TPatient;

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
    

    public MEvaluationHall(int EVT) {
        this.rl = new ReentrantLock();
        this.rooms = new TPatient[nRooms];

        this.cNurse = new Condition[nRooms];
        this.cPatient = new Condition[nRooms];
        this.bPatient = new boolean[nRooms];
        this.bNurse = new boolean[nRooms];

        for (int i=0; i<nRooms; i++){
            this.cNurse[i] = this.rl.newCondition();
            this.cPatient[i] = this.rl.newCondition();
            this.bPatient[i] = false;
            this.bNurse[i] = false;
        }
    }

    public void enterHall(TPatient patient) {
        int chosenEVR = 0;

        patient.tSleep();

        System.out.println(String.format("a tentar entrar no evh %s",patient));

        try {
            rl.lock();

            for (int i=0; i<nRooms; i++) {
                if (rooms[i] == null) {
                    chosenEVR = i;
                    break;
                }
            }

            System.out.println(String.format("entrou no EVR%d %s", chosenEVR+1, patient));
            patient.log(String.format("EVR%d", chosenEVR+1));

            rooms[chosenEVR] = patient;

            bNurse[chosenEVR] = true;
            cNurse[chosenEVR].signal();

            bPatient[chosenEVR] = false;
            while ( !bPatient[chosenEVR] )
                cPatient[chosenEVR].await();

            rooms[chosenEVR] = null;

        } catch (InterruptedException e) {
                e.printStackTrace();
        } finally {
            rl.unlock();
        }

        exitHall(patient);
    }


    public void work(TNurse nurse) {
        int roomId = nurse.getRoomId();
        TPatient patient = null;
        
        while (true) {
            try {
                rl.lock();

                bNurse[roomId] = false;
                while ( !bNurse[roomId] )
                    cNurse[roomId].await();

                patient = rooms[roomId];
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                rl.unlock();
            }

            nurse.evaluate( patient );
            patient.log(String.format("EVR%d", roomId+1));

            try {
                rl.lock();
                bPatient[roomId] = true;
                cPatient[roomId].signal();
            } finally {
                rl.unlock();
            }

        }
    }
    
    public void exitHall(TPatient patient) {
        patient.notifyExit("EVH");
        System.out.println(String.format("saiu do evh %s", patient));
    }
}
