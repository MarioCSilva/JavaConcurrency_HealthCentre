package HC.Entities;

import HC.EntranceHall.IEntranceHall_Patient;
import HC.EvaluationHall.IEvaluationHall_Patient;

public class TChildPatient extends TPatient {
    public TChildPatient(int patientId, int ttm, IEntranceHall_Patient mEntranceHall, IEvaluationHall_Patient mEvaluationHall ) {
        super(patientId, ttm, false, mEntranceHall, mEvaluationHall);
    }
}