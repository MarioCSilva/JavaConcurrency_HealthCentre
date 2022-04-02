package HC.Entities;

import HC.EntranceHall.MEntranceHall;

public class TChildPatient extends TPatient {
    public TChildPatient(int patientId, MEntranceHall mEntranceHall) {
        super(patientId, false, mEntranceHall);
    }
}