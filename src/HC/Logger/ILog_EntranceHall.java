package HC.Logger;

import HC.Entities.TPatient;

public interface ILog_EntranceHall {
    void writePatient(TPatient patient, String room);
}
