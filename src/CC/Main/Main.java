package CC.Main;

import CC.Communication.Client;
import CC.ControllerGUI.ControllerGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        final String HCHostName = "localhost";
        final int HCPort = 8080;

        Client client = new Client(HCHostName, HCPort);
        client.start();

        ControllerGUI controllerGUI = new ControllerGUI(client);

        SwingUtilities.invokeLater(() -> {
            GUICC GUICC = new GUICC(controllerGUI);
            JFrame jf = new JFrame();
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jf.setSize(800, 500);
            jf.add(GUICC.getPanel1());
            jf.setVisible(true);
        });
    }
}
