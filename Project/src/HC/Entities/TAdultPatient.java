package HC.Entities;

import HC.CallCentreHall.ICallCentreHall_Patient;
import HC.EntranceHall.IEntranceHall_Patient;
import HC.EvaluationHall.IEvaluationHall_Patient;
import HC.Controller.IController_Patient;
import HC.MedicalHall.IMedicalHall_Patient;
import HC.PaymentHall.IPaymentHall_Patient;
import HC.WaitingHall.IWaitingHall_Patient;

public class TAdultPatient extends TPatient {
    public TAdultPatient(int patientId, int ttm, IController_Patient controller, ICallCentreHall_Patient mCallCentreHall,
                         IEntranceHall_Patient mEntranceHall, IEvaluationHall_Patient mEvaluationHall,
                         IWaitingHall_Patient mWaitingHall, IMedicalHall_Patient mMedicalHall, IPaymentHall_Patient mPaymentHall) {
        super(patientId, ttm, true, controller, mCallCentreHall, mEntranceHall, mEvaluationHall, mWaitingHall, mMedicalHall, mPaymentHall);
    }
}