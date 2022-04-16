package CC.Main;

import CC.Communication.Client;
import CC.Controller.Controller;

import javax.swing.*;

public class Main {
    public static void main(String[] args){
        final String HCHostName = "localhost";
        final int HCPort = 8080;

        Client client = new Client(HCHostName,HCPort);
        client.start();

        Controller controller = new Controller(client);

        SwingUtilities.invokeLater(() -> {
            GUICC GUICC = new GUICC(controller);
            JFrame jf = new JFrame();
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jf.setSize(800, 500);
            jf.add(GUICC.getPanel1());
            jf.setVisible(true);
        });
    }
}
