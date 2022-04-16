package HC.Main;

import HC.Controller.Controller;
import HC.Communication.Server;
import HC.Logger.MLog;

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
        Controller controller = new Controller(GUI);
        final MLog logger;
        try {
            logger = new MLog(controller);
            Server server = new Server(HCPort, logger);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
