package HC.EntranceHall;


import HC.CallCentreHall.ICallCentreHall_EntranceHall;
import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.concurrent.locks.ReentrantLock;

public class MEntranceHall implements IEntranceHall_CallCenter, IEntranceHall_Patient {
    private final MFIFO ETR1_FIFO;
    private final MFIFO ETR2_FIFO;
    private int ETN = 0;
    
    private final ReentrantLock entranceLock;
    private final ICallCentreHall_EntranceHall cch;

    public MEntranceHall(int nos, ICallCentreHall_EntranceHall cch) {
        this.ETR1_FIFO = new MFIFO(nos);
        this.ETR2_FIFO = new MFIFO(nos);
        this.entranceLock = new ReentrantLock();
        this.cch = cch;
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

        cch.notifyETHEntrance(patient);

        // assign the patient to a room
        if (patient.getIsAdult()) {
            ETR2_FIFO.put(patient.getETN());
        } else {
            ETR1_FIFO.put(patient.getETN());
        }

        // TODO:
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