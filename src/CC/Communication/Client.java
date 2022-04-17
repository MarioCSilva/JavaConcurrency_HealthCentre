package CC.Communication;

import HC.Communication.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

// Client class
public class Client extends Thread {
    private final String serverHostName;
    private final int serverPort;

    private Socket clientSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private SocketAddress serverAddress;

    public Client(String serverHostName, int serverPort) {
        this.serverHostName = serverHostName;
        this.serverPort = serverPort;
    }

    public void run() {
        try {
            clientSocket = new Socket();
            this.serverAddress = new InetSocketAddress(serverHostName, serverPort);
            clientSocket.connect(serverAddress);

            // get the output stream of the client
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            // get the input stream of the client
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    /** 
     * @param msg
     */
    public void sendMsg(Message msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}