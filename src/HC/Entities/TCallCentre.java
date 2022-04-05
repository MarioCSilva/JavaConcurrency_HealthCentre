package HC.Entities;


import HC.EntranceHall.IEntranceHall_CallCenter;

public class TCallCentre extends Thread {
    private int ETHNumPatients = 0;
    private int EVHNumPatients = 0;
    private final IEntranceHall_CallCenter eth;

    public TCallCentre(IEntranceHall_CallCenter eth) {
        this.eth = eth;
    }

    public void notifyETHEntrance() {
        this.ETHNumPatients++;

        this.callETHPatient();
    }

    private void callETHPatient() {
        if (EVHNumPatients > 0) {
            eth.exitHall();
        }
    }
}
