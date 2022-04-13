package HC.Entities;

import HC.EntranceHall.IEntranceHall_Patient;
import HC.EvaluationHall.IEvaluationHall_Patient;

public class TAdultPatient extends TPatient {
    public TAdultPatient(int patientId, int ttm, IEntranceHall_Patient mEntranceHall, IEvaluationHall_Patient mEvaluationHall) {
        super(patientId, ttm, true, mEntranceHall,mEvaluationHall );
    }
}