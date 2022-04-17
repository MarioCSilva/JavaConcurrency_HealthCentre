package CC.ControllerGUI;


import CC.Communication.Client;
import HC.Communication.Message;
import HC.Enumerates.MessageTopic;

public class ControllerGUI {
    private final Client client;
    private boolean isManualMode;

    public ControllerGUI(Client client) {
        this.client = client;
        this.isManualMode = false;
    }

    public void startSimulation(int numberOfAdults, int numberOfChildren, int nos, int evt, int mdt, int pyt, int ttm) {
        client.sendMsg(
                new Message(
                        MessageTopic.START,
                        numberOfAdults,
                        numberOfChildren,
                        nos,
                        evt,
                        mdt,
                        pyt,
                        ttm
                ));
        System.out.println("Starting Simulation...");
    }

    public void suspendSimulation() {
        client.sendMsg(
                new Message(
                        MessageTopic.SUSPEND
                ));
        System.out.println("Simulation Suspended");
    }

    public void resumeSimulation() {
        client.sendMsg(
                new Message(
                        MessageTopic.RESUME
                ));
        System.out.println("Resumed Simulation");
    }

    public void stopSimulation() {
        client.sendMsg(
                new Message(
                        MessageTopic.STOP
                ));
        System.out.println("Stopped Simulation");
    }

    public void endSimulation() {
        client.sendMsg(
                new Message(
                        MessageTopic.END
                ));
        System.out.println("Ended Simulation");
        System.exit(0);
    }

    public void changeOperatingMode() {
        System.out.println("Changed Operating mode");
        this.isManualMode = ! this.isManualMode;
        client.sendMsg(
                new Message(
                        MessageTopic.MODE
                ));
    }
}
