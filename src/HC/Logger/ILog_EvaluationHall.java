package HC.Logger;

import HC.Entities.TPatient;

public interface ILog_EvaluationHall {
    void writePatient(TPatient patient, String room);
}
