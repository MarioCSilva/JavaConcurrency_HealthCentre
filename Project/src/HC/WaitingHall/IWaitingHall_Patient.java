package HC.WaitingHall;

import HC.Entities.TPatient;

import java.io.IOException;

public interface IWaitingHall_Patient {
    void enterHall(TPatient patient) throws InterruptedException, IOException;
}
