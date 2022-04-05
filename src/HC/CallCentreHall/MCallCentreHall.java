package HC.CallCentreHall;

import HC.Entities.TCallCentre;

import java.util.concurrent.locks.ReentrantLock;

public class MCallCentreHall {
    private int ETHNumPatients;
    private int EVHNumPatients;
    private ReentrantLock lock1;
    private final TCallCentre cc;


    public MCallCentreHall(TCallCentre cc) {
        this.cc = cc;
        this.ETHNumPatients = 0;
        this.EVHNumPatients = 0;

        this.lock1 = new ReentrantLock();
    }

    public void notifyETHEntrance() {
        try{
            lock1.lock();
            this.ETHNumPatients++;
        } catch () {}
        finally {
            lock1.unlock();
            this.cc.signal();
        }
    }

}
