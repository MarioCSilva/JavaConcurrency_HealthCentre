package HC.Main;

import HC.Communication.Server;
import HC.ControllerGUI.ControllerGUI;
import HC.Controller.MController;

import javax.swing.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        final int HCPort = 8080;

        GUIHC GUI = new GUIHC();
        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(800, 500);
        jf.add(GUI.getPanel1());
        jf.setVisible(true);
        ControllerGUI controllerGUI = new ControllerGUI(GUI);
        final MController controller;
        try {
            controller = new MController(controllerGUI);
            Server server = new Server(HCPort, controller);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
