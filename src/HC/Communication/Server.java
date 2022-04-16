package HC.Communication;


import HC.Entities.TClientHandler;
import HC.Logger.MLog;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private int port;
    private ServerSocket serverSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private MLog logger;

    public Server(int port, MLog logger) {
        this.port = port;
        this.logger = logger;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println(serverSocket.getInetAddress());
            System.out.println(serverSocket.getLocalPort());
            // running infinite loop for getting
            // client request
            while (true) {
                // socket object to receive incoming client
                // requests
                Socket client = serverSocket.accept();

                // Displaying that new client is connected
                // to server
                System.out.println("New client connected");

                // create a new thread object
                TClientHandler clientSock = new TClientHandler(client, logger);

                // This thread will handle the client
                // separately
                new Thread(clientSock).start();
            }
        }
        catch(Exception e) {
            System.err.println(e);
        }
    }

    public void close() {
        try {
            serverSocket.close();
        }
        catch(Exception e) {
            System.err.println(e);
        }
    }
}

