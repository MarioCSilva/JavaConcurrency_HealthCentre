package HC.EntranceHall;


import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.concurrent.locks.ReentrantLock;

public class MEntranceHall implements IEntranceHall_CallCenter, IEntranceHall_Patient {
    private int ETN = 0;
    private final ReentrantLock exitLock;
    private final ReentrantLock ETNLock;

    private final MFIFO ETR1_FIFO;
    private final MFIFO ETR2_FIFO;

    public MEntranceHall(int nos) {
        this.exitLock = new ReentrantLock();
        this.ETNLock = new ReentrantLock();
        this.ETR1_FIFO = new MFIFO( nos/2 );
        this.ETR2_FIFO = new MFIFO( nos/2 );
    }

    public void enterHall(TPatient patient) {
        // give an ETN to each patient upon entering ETH
        try {
            ETNLock.lock();
            patient.setETN(ETN);
            ETN++;
        } finally {
            ETNLock.unlock();
        }

        patient.log("ETH");

        // assign the patient to a room
        if (patient.getIsAdult()) {
            ETR2_FIFO.put(patient, "ETH", "ET2");
        } else {
            ETR1_FIFO.put(patient, "ETH", "ET1");
        }
    }

    public void exitHall() {
        TPatient ETN_ETR1;
        TPatient ETN_ETR2;

        ETN_ETR1 = this.ETR1_FIFO.getHead();
        ETN_ETR2 = this.ETR2_FIFO.getHead();

        System.out.println(ETN_ETR1);
        System.out.println(ETN_ETR2);

        if (ETN_ETR1 == null && ETN_ETR2 == null) {
            return;
        } else if (ETN_ETR1 != null && ETN_ETR2 == null) {
            this.ETR1_FIFO.get();
        } else if (ETN_ETR1 == null) {
            this.ETR2_FIFO.get();
        } else if (ETN_ETR1.getETN() < ETN_ETR2.getETN()) {
            this.ETR1_FIFO.get();
        } else {
            this.ETR2_FIFO.get();
        }
    }
}