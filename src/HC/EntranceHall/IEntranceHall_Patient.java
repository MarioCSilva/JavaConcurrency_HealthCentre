package HC.EntranceHall;

import HC.Entities.TPatient;

import java.io.IOException;

public interface IEntranceHall_Patient {
    void enterHall(TPatient patient) throws InterruptedException, IOException;
}