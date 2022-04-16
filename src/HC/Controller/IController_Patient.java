package HC.Controller;

import HC.Entities.TPatient;

import java.io.IOException;

public interface IController_Patient {
    void writePatientMovement(TPatient tPatient, String room) throws InterruptedException, IOException;

    void checkSuspend() throws InterruptedException;
}
