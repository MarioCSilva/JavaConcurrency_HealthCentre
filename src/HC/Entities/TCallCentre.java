package HC.Entities;


import HC.EntranceHall.IEntranceHall_CallCenter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TCallCentre extends Thread {
    private final IEntranceHall_CallCenter eth;

    private final ReentrantLock rl;
    private final Condition run;

    public TCallCentre(IEntranceHall_CallCenter eth) {
        this.eth = eth;
        this.rl = new ReentrantLock();
        this.run = rl.newCondition();
    }

    public Condition getRunCondition(){
        return run;
    }
    @Override
    public void run() {
        while (true) {
            try {
                this.run.await();


                if (true) {
                    this.callETHPatient();
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void callETHPatient() {
        eth.exitHall();
    }
}
