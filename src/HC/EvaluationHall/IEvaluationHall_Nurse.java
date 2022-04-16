package HC.EvaluationHall;

import HC.Entities.TNurse;

import java.io.IOException;

public interface IEvaluationHall_Nurse {
    void work(TNurse tNurse) throws InterruptedException, IOException;
}   