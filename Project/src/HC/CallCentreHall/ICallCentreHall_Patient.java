package HC.CallCentreHall;

import HC.Entities.TPatient;

public interface ICallCentreHall_Patient {
    void notifyEntrance(TPatient patient, String hall);

    void notifyExit(TPatient patient, String hall);
}
