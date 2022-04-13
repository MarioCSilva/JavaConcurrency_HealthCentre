package HC.EvaluationHall;

import HC.CallCentreHall.ICallCentreHall_EvaluationHall;
import HC.Entities.Nurse;
import HC.Entities.TPatient;

import HC.FIFO.MFIFO;
import HC.Logger.ILog_EvaluationHall;

import java.util.concurrent.locks.ReentrantLock;

public class MEvaluationHall implements IEvaluationHall_Patient {
    private final MFIFO EVRFifos[];
    private final Nurse EVRNurses[];
    private final ReentrantLock entranceLock;
    private final int nRooms = 4;
    private final ICallCentreHall_EvaluationHall cch;
    private final ILog_EvaluationHall logger;
    private final int nos;


    public MEvaluationHall(ILog_EvaluationHall logger, int nos, int EVT, ICallCentreHall_EvaluationHall cch) {
        this.logger = logger;
        this.cch = cch;
        this.entranceLock = new ReentrantLock();
        this.EVRFifos = new MFIFO[nRooms];
        this.EVRNurses = new Nurse[nRooms];
        this.nos = nos;
        for (int i=0; i<nRooms; i++){
            this.EVRFifos[i] = new MFIFO(nos, false);
            this.EVRNurses[i] = new Nurse(EVT);
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

        logger.writePatient(patient, String.format("EVR%d", chosenEVR+1));

        EVRFifos[chosenEVR].put(patient);

        System.out.println(String.format("saiu do fifo %s", patient));

        EVRNurses[chosenEVR].evaluate(patient);

        System.out.println(String.format("avaliado %s",patient));
        
        logger.writePatient(patient, String.format("EVR%d", chosenEVR+1));

        exitHall(patient, EVRFifos[chosenEVR]);
    }

    public void exitHall(TPatient patient, MFIFO EVRFifo) {
        EVRFifo.getPatient(patient);

        System.out.println(String.format("saiu do evh %s", patient));

        cch.notifyEVHExit();
    }
}
