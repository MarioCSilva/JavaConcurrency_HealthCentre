package HC.EntranceHall;


import HC.CallCentreHall.ICallCentreHall_EntranceHall;
import HC.Entities.TPatient;
import HC.FIFO.MFIFO;
import HC.Logger.ILog_EntranceHall;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MEntranceHall implements IEntranceHall_CallCenter, IEntranceHall_Patient {
    private int ETN = 0;
    private final ReentrantLock exitLock;
    private final ReentrantLock ETNLock;

    private final ICallCentreHall_EntranceHall cch;

    private final ILog_EntranceHall logger;

    private final MFIFO ETR1_FIFO;
    private final MFIFO ETR2_FIFO;
    private final int size;

    public MEntranceHall(ILog_EntranceHall logger, int nos, ICallCentreHall_EntranceHall cch) {
        this.logger = logger;
        this.exitLock = new ReentrantLock();
        this.ETNLock = new ReentrantLock();
        this.cch = cch;

        this.size = nos;
        this.ETR1_FIFO = new MFIFO( size );
        this.ETR2_FIFO = new MFIFO( size );
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

        logger.writePatient(patient, "ETH");

        // assign the patient to a room
        if (patient.getIsAdult()) {
            logger.writePatient(patient, "ET2");
            ETR2_FIFO.put(patient, cch);
        } else {
            logger.writePatient(patient, "ET1");
            ETR1_FIFO.put(patient, cch);
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