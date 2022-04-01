package CC.Controller;


import CC.Communication.Client;
import HC.Communication.Message;
import HC.Enumerates.MessageTopic;

public class Controller {
    private final Client client;

    public Controller(Client client) {
        this.client = client;
    }

    public void startSimulation() {
        System.out.println("Starting Simulation...");
        client.sendMsg(new Message(MessageTopic.START));
    }

    public void suspendSimulation() {
        System.out.println("Simulation Suspended");
    }

    public void resumeSimulation() {
        System.out.println("Resumed Simulation");
    }

    public void stopSimulation() {
        System.out.println("Stopped Simulation");
    }

    public void endSimulation() {
        System.out.println("Ended Simulation");
    }

    public void changeOperatingMode(){
        System.out.println("Changed Operating mode");
    }
}
