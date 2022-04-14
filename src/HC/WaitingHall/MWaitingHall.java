package HC.WaitingHall;

import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.concurrent.locks.ReentrantLock;

public class MWaitingHall implements IWaitingHall_CallCenter, IWaitingHall_Patient {

    private final ReentrantLock entranceLock;
    private int WTN = 0;

    private final MFIFO WTR1_FIFO;
    private final MFIFO WTR2_FIFO;

    public MWaitingHall(int nos){
        this.entranceLock = new ReentrantLock();
        this.WTR1_FIFO = new MFIFO(nos / 2);
        this.WTR2_FIFO = new MFIFO(nos / 2);
    }

    public void enterHall(TPatient patient){
        try {
            entranceLock.lock();
            patient.setWTN(WTN);
            WTN++;
        } finally {
            entranceLock.unlock();
        }

        patient.log("WTH");

        if (patient.getIsAdult()) {
            WTR2_FIFO.put(patient, "WWH", "WTR2");
        } else {
            WTR1_FIFO.put(patient, "WWH", "WTR1");
        }
    }
}
