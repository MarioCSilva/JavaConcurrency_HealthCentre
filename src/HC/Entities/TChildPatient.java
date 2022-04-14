package HC.Entities;

import HC.CallCentreHall.ICallCentreHall_Patient;
import HC.EntranceHall.IEntranceHall_Patient;
import HC.EvaluationHall.IEvaluationHall_Patient;
import HC.Logger.ILog_Patient;
import HC.WaitingHall.IWaitingHall_Patient;

public class TChildPatient extends TPatient {
    public TChildPatient(int patientId, int ttm, ILog_Patient logger, ICallCentreHall_Patient mCallCentreHall,
                         IEntranceHall_Patient mEntranceHall, IEvaluationHall_Patient mEvaluationHall,
                         IWaitingHall_Patient mWaitingHall) {
        super(patientId, ttm, false, logger, mCallCentreHall, mEntranceHall, mEvaluationHall, mWaitingHall);
    }
}