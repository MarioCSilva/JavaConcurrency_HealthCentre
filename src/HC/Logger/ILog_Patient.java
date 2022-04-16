package HC.Logger;

import HC.Entities.TPatient;

public interface ILog_Patient {
    void writePatient(TPatient patient, String room);

    void writePatientMovement(TPatient patient, String roomExiting, String roomEntering);
}
