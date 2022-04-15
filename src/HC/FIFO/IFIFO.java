package HC.FIFO;

import HC.Entities.TPatient;

public interface IFIFO {
    int put(TPatient patient);
    int get();
}
