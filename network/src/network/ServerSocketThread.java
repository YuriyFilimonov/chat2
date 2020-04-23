package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;

public class ServerSocketThread extends Thread {

    private final int port;
    private final int timeout;
    ServerSocketThreadListener listener;
    public Vector<SocketThread> socketThreadVector;

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;
        socketThreadVector = new Vector<>();
        start();
    }

    public Vector<SocketThread> getSocketThreadVector() {
        return socketThreadVector;
    }

    @Override
    public void run() {
        listener.onServerStarted(this);
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(timeout);
            listener.onServerCreated(this, server);
            while (!isInterrupted()) {
                SocketThread st;
                Socket s;
                try {
                    s = server.accept();
                } catch (SocketTimeoutException e) {
                    listener.onServerTimeout(this, server);
                    continue;
                }
                st = listener.onSocketAccepted(this, server, s);
                socketThreadVector.addElement(st);
            }
        } catch (IOException e) {
            listener.onServerException(this, e);
        } finally {
            listener.onServerStop(this);
        }
    }
}
