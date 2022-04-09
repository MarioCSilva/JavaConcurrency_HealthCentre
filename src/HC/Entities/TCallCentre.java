package HC.Entities;


import HC.CallCentreHall.ICallCentreHall_CallCentre;
import HC.EntranceHall.IEntranceHall_CallCenter;


public class TCallCentre extends Thread {
    private final IEntranceHall_CallCenter eth;
    private final ICallCentreHall_CallCentre cch;


    public TCallCentre(IEntranceHall_CallCenter eth, ICallCentreHall_CallCentre cch) {
        this.eth = eth;
        this.cch = cch;
    }

    @Override
    public void run() {
        while (true) {
            cch.work(this);
        }
    }

    public void callETHPatient() {
        eth.exitHall();
    }
}
