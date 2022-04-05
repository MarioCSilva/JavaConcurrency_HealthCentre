package HC.Entities;

import HC.CallCentreHall.ICallCentreHall_Patient;
import HC.EntranceHall.IEntranceHall_Patient;

public class TPatient extends Thread {
    private final int patientId;
    private final IEntranceHall_Patient mEntranceHall;
    private final ICallCentreHall_Patient mCallCentreHall;
    private final boolean isAdult;
    private int ETN;

    public TPatient(int patientId, boolean isAdult, IEntranceHall_Patient mEntranceHall,
                    ICallCentreHall_Patient mCallCentreHall) {
        this.patientId = patientId;
        this.mEntranceHall = mEntranceHall;
        this.isAdult = isAdult;
        this.mCallCentreHall = mCallCentreHall;
    }

    @Override
    public void run() {
        // enter the ETH
        this.mEntranceHall.enterHall();

        // notify call center the entrance on ETH
        this.mCallCentreHall.notifyETHEntrance();
    }

    public boolean getIsAdult(){
        return this.isAdult;
    }

    public int getPatientId() {
        return this.patientId;
    }

    public void setETN(int ETN) {
        this.ETN = ETN;
    }

    public int getETN() {
        return this.ETN;
    }
}
