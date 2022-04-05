package HC.EntranceHall;


import HC.Entities.TCallCentre;
import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.concurrent.locks.ReentrantLock;

public class MEntranceHall {
    private final MFIFO ETR1_FIFO;
    private final MFIFO ETR2_FIFO;
    private int ETN = 0;
    private final ReentrantLock entranceLock;
    private final TCallCentre cc;

    public MEntranceHall(int ETR1_nos, int ETR2_nos, TCallCentre cc) {
        this.ETR1_FIFO = new MFIFO(ETR1_nos);
        this.ETR2_FIFO = new MFIFO(ETR2_nos);
        this.entranceLock = new ReentrantLock();
        this.cc = cc;
    }

    public void enterHall(TPatient patient) {
        // give an ETN to each patient upon entering ETH
        try {
            entranceLock.lock();
            patient.setETN(ETN);
            ETN++;
        } finally {
            entranceLock.unlock();
        }

        // assign the patient to a room
        if (patient.getIsAdult()) {
            ETR2_FIFO.put(patient.getETN());
        } else {
            ETR1_FIFO.put(patient.getETN());
        }

        // sleep(ttm);
    }

    public void exitHall() {
        int ETN_ETR1 = this.ETR1_FIFO.getHead();
        int ETN_ETR2 = this.ETR2_FIFO.getHead();

        if (ETN_ETR1 < ETN_ETR2) {
            this.ETR1_FIFO.get();
        } else {
            this.ETR2_FIFO.get();
        }
    }
}