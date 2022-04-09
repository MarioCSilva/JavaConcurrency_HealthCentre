package HC.CallCentreHall;

import HC.Entities.TCallCentre;
import HC.Entities.TPatient;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MCallCentreHall implements ICallCentreHall_EntranceHall,
                                        ICallCentreHall_CallCentre {
    private int ETHNumPatients;
    private int EVHNumPatients;
    private ReentrantLock lock1;
    private Condition cAwakeCC;
    private boolean bAwakeCC;


    public MCallCentreHall() {
        this.ETHNumPatients = 0;
        this.EVHNumPatients = 0;
        this.lock1 = new ReentrantLock();
        this.cAwakeCC = lock1.newCondition();
        this.bAwakeCC = false;
    }

    public void notifyETHEntrance(TPatient patient) {
        try {
            lock1.lock();
            this.ETHNumPatients++;

            this.bAwakeCC = true;
            this.cAwakeCC.signal();
        } catch (Exception e) {}
        finally {
            lock1.unlock();
        }
    }

    public void work(TCallCentre cc) {
        try {
            lock1.lock();

            while ( !this.bAwakeCC )
                this.cAwakeCC.await();

            // TODO: some condition to call patients
            if (EVHNumPatients == 32103201 && ETHNumPatients > 0) {
                cc.callETHPatient();
            }

            this.bAwakeCC = false;

        } catch (Exception e) {}
        finally {
            lock1.unlock();
        }
    }
}
