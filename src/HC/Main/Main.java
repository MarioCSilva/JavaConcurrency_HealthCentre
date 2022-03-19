package HC.Main;

import HC.Communication.Server;

public class Main {

    public static void main(String[] args) {
        final int HCPort = 8001;
        Server server = new Server(HCPort);
        server.start();
    }
}
