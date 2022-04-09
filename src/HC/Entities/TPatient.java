package HC.Entities;

import HC.EntranceHall.IEntranceHall_Patient;

public class TPatient extends Thread {
    private final int patientId;
    private final IEntranceHall_Patient mEntranceHall;
    private final boolean isAdult;
    private int ETN;

    public TPatient(int patientId, boolean isAdult, IEntranceHall_Patient mEntranceHall) {
        this.patientId = patientId;
        this.mEntranceHall = mEntranceHall;
        this.isAdult = isAdult;
    }

    @Override
    public void run() {
        // enter the ETH
        this.mEntranceHall.enterHall(this);
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
