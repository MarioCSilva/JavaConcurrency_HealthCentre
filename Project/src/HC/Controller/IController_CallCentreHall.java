package HC.Controller;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public interface IController_CallCentreHall {
    ReentrantLock getCCHLock();

    Condition getCAwakeCC();

    void setBAwakeCC(boolean b);

    boolean getBAwakeCC();
}
