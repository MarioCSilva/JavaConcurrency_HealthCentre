package HC.Entities;

import HC.EntranceHall.IEntranceHall_Patient;

public class TAdultPatient extends TPatient {
    public TAdultPatient(int patientId, IEntranceHall_Patient mEntranceHall) {
        super(patientId, true, mEntranceHall);
    }
}