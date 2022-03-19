package CC.Main;

import CC.Controller.Client;

public class Main {
    public static void main(String[] args){
        Client client = new Client("localhost",37195);
        client.run();
    }
}
