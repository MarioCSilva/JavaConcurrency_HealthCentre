package HC.Entities;

import HC.EntranceHall.MEntranceHall;

public class TAdultPatient extends TPatient {
    public TAdultPatient(int patientId, MEntranceHall mEntranceHall) {
        super(patientId, true, mEntranceHall);
    }
}