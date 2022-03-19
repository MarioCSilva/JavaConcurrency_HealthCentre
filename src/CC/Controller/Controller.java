package CC.Controller;


public class Controller {

    private final String host;
    private final Integer port;

    public Controller (String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public void startSimulation() {
        System.out.println("Starting Simulation...");
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
