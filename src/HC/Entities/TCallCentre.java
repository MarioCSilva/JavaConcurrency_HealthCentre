package HC.Entities;


import HC.CallCentreHall.ICallCentreHall_CallCentre;
import HC.EntranceHall.IEntranceHall_CallCentre;
import HC.MedicalHall.IMedicalHall_CallCentre;
import HC.WaitingHall.IWaitingHall_CallCentre;


public class TCallCentre extends Thread {
    private final IEntranceHall_CallCentre eth;
    private final ICallCentreHall_CallCentre cch;
    private final IWaitingHall_CallCentre wth;
    private final IMedicalHall_CallCentre mdh;


    public TCallCentre(IEntranceHall_CallCentre eth, ICallCentreHall_CallCentre cch,
                       IWaitingHall_CallCentre wth, IMedicalHall_CallCentre mdh) {
        this.eth = eth;
        this.cch = cch;
        this.wth = wth;
        this.mdh = mdh;
    }

    @Override
    public void run() {
        cch.work(this);
    }

    public void callETHPatient() {
        eth.exitHall();
    }

    public void callWTHAPatient() {
        wth.exitHall("A");
    }

    public void callWTHCPatient() {
        wth.exitHall("C");
    }

    public void callMDWAPatient() {
        mdh.exitWaitingRoom("A");
    }

    public void callMDWCPatient() {
        mdh.exitWaitingRoom("C");
    }

}
