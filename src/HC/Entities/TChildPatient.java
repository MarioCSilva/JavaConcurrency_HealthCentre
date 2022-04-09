package HC.Entities;

import HC.EntranceHall.IEntranceHall_Patient;

public class TChildPatient extends TPatient {
    public TChildPatient(int patientId, IEntranceHall_Patient mEntranceHall) {
        super(patientId, false, mEntranceHall);
    }
}