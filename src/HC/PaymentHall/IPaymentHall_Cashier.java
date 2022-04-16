package HC.PaymentHall;

import HC.Entities.TCashier;

import java.io.IOException;

public interface IPaymentHall_Cashier {
    void work(TCashier cashier) throws InterruptedException, IOException;
}
