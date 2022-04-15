package HC.Entities;


import HC.CallCentreHall.ICallCentreHall_CallCentre;
import HC.EntranceHall.IEntranceHall_CallCentre;
import HC.WaitingHall.IWaitingHall_CallCentre;


public class TCallCentre extends Thread {
    private final IEntranceHall_CallCentre eth;
    private final ICallCentreHall_CallCentre cch;
    private final IWaitingHall_CallCentre wth;


    public TCallCentre(IEntranceHall_CallCentre eth, ICallCentreHall_CallCentre cch, IWaitingHall_CallCentre wth) {
        this.eth = eth;
        this.cch = cch;
        this.wth = wth;
    }

    @Override
    public void run() {
        cch.work(this);
    }

    public void callETHPatient() {
        eth.exitHall();
    }

    public void callWTHPatient() {
        wth.exitHall();
    }
}
