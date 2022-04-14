package HC.Logger;

import HC.Entities.TPatient;

public interface ILog_WaitingHall {
    void writePatient(TPatient patient, String room);
}
