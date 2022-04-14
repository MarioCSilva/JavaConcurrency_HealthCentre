package HC.EvaluationHall;

import HC.Entities.TNurse;
import HC.Entities.TPatient;

import HC.FIFO.MFIFO;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MEvaluationHall implements IEvaluationHall_Patient, IEvaluationHall_Nurse {
    private final MFIFO EVRFifos[];
    private final ReentrantLock entranceLock;
    private final ReentrantLock nurseLock;
    private final Condition cPatient[];
    private final Condition cNurse[];
    private final boolean bNurse[];
    private final boolean bPatient[];
    private final int nRooms = 4;
    private final int nos;


    public MEvaluationHall(int nos, int EVT) {
        this.entranceLock = new ReentrantLock();
        this.nurseLock = new ReentrantLock();
        this.EVRFifos = new MFIFO[nRooms];

        this.cNurse = new Condition[nRooms];
        this.cPatient = new Condition[nRooms];
        this.bPatient = new boolean[nRooms];
        this.bNurse = new boolean[nRooms];

        this.nos = nos;

        for (int i=0; i<nRooms; i++){
            cNurse[i] = nurseLock.newCondition();
            cPatient[i] = nurseLock.newCondition();
            bPatient[i] = false;
            bNurse[i] = false;
            this.EVRFifos[i] = new MFIFO(nos, false);
        }
    }

    public void enterHall(TPatient patient) {
        int chosenEVR = 0;
        System.out.println(String.format("a tentar entrar no evh %s",patient));

        try {
            entranceLock.lock();
            int min = nos;
            for (int i=0; i<nRooms; i++) {
                if (EVRFifos[i].getCount() < min) {
                    min = EVRFifos[i].getCount();
                    chosenEVR = i;
                }
            }
        } finally {
            entranceLock.unlock();
        }


        System.out.println(String.format("entrou no EVR%d %s", chosenEVR+1, patient));
        patient.log(String.format("EVR%d", chosenEVR+1));


        EVRFifos[chosenEVR].put(patient);
        System.out.println(String.format("saiu do fifo %s", patient));

        try {
            nurseLock.lock();

            bNurse[chosenEVR] = true;
            cNurse[chosenEVR].signal();

            bPatient[chosenEVR] = false;
            while ( !bPatient[chosenEVR] )
                cPatient[chosenEVR].await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            nurseLock.unlock();
        }

        patient.log(String.format("EVR%d", chosenEVR+1));

        exitHall(patient);
    }


    public void work(TNurse nurse) {
        int roomId = nurse.getRoomId();
        while (true) {
            try {
                nurseLock.lock();

                bNurse[roomId] = false;
                while ( !bNurse[roomId] )
                    cNurse[roomId].await();

                TPatient patient = EVRFifos[roomId].getHead();

                nurse.evaluate( patient );

                EVRFifos[roomId].get();

                bPatient[roomId] = true;
                cPatient[roomId].signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                nurseLock.unlock();
            }
        }
    }


    public void exitHall(TPatient patient) {
        patient.notifyExit();

        System.out.println(String.format("saiu do evh %s", patient));
    }
}
