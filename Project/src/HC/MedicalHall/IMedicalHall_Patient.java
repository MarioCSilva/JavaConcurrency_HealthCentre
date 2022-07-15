package HC.MedicalHall;

import HC.Entities.TPatient;

import java.io.IOException;

public interface IMedicalHall_Patient {
    void enterHall(TPatient patient) throws InterruptedException, IOException;
}