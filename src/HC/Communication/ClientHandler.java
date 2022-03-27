package HC.Communication;

import HC.Enumerates.MessageTopic;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;

    // Constructor
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void startSimulation() {
        // start all classes call center, entrance hall
        // initiate patients
    }

    public void run() {
        try {
            // get the output stream of client
            out = new ObjectOutputStream(clientSocket.getOutputStream());

            // get the input stream of client
            in = new ObjectInputStream(clientSocket.getInputStream());
            Message msg;
            while ((msg = (Message) in.readObject()) != null) {
                System.out.printf(
                        " Sent from the client: %s\n",
                        msg.getTopic());
                MessageTopic topic = msg.getTopic();
                if (topic == MessageTopic.START) {
                    System.out.println("Starting simulation");
                    // start a new simulation
                    startSimulation();
                    // Patients are created and go to the Entrance Hall
                } else if (topic == MessageTopic.SUSPEND) {
                    // to suspend the running simulation
                    // Patients, Call Center and Cashier suspend their activity
                } else if (topic == MessageTopic.RESUME) {
                    // to resume the running simulation
                    // Patients, Call Centre and Cashier resume their normal activity
                } else if (topic == MessageTopic.STOP) {
                    // to stop the simulation
                    // simulation evolves to its initial state and all Patients die
                } else if (topic == MessageTopic.END) {
                    // to end the simulation
                    // the two processes end
                } else if (topic == MessageTopic.MODE) {
                    // option = {manual, auto}
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
