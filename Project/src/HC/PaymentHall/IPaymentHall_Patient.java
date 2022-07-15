package HC.PaymentHall;

import HC.Entities.TPatient;

import java.io.IOException;

public interface IPaymentHall_Patient {
    void enterHall(TPatient patient) throws InterruptedException, IOException;
}
