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

    public void run() {
        try {
            // get the output stream of client
            out = new ObjectOutputStream(clientSocket.getOutputStream());

            // get the input stream of client
            in = new ObjectInputStream(clientSocket.getInputStream());
            Message msg;
            while ((msg = (Message) in.readObject()) != null) {
                // writing the received message from
                // client
                System.out.printf(
                        " Sent from the client: %s\n",
                        msg.getTopic());
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
