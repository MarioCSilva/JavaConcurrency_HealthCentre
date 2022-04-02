package HC.Entities;

import HC.EntranceHall.MEntranceHall;

public class TPatient extends Thread {
    private final int patientId;
    private final MEntranceHall mEntranceHall;
    private final boolean isAdult;
    private int ETN;

    public TPatient(int patientId, boolean isAdult, MEntranceHall mEntranceHall) {
        this.patientId = patientId;
        this.mEntranceHall = mEntranceHall;
        this.isAdult = isAdult;
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
