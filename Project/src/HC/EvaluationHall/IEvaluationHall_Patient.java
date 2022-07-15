package HC.EvaluationHall;

import HC.Entities.TPatient;

import java.io.IOException;

public interface IEvaluationHall_Patient {
    void enterHall(TPatient patient) throws InterruptedException, IOException;
}
