import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMain {
    private static final int TCP_PORT = 5555;
    private static final int UDP_PORT = 5556;
    private static ConcurrentHashMap<Integer, ClientThread> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            UDPThread udpThread = new UDPThread(UDP_PORT);
            new Thread(String.valueOf(udpThread)).start();

            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("Server started on TCP port " + TCP_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = generateClientId();
                System.out.println("New client connected. ID: " + clientId);

                ClientThread clientThread = new ClientThread(clientSocket, clientId, udpThread);
                activeClients.put(clientId, clientThread);
                new Thread(clientThread).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static int generateClientId() {
        return (int) (Math.random() * 1000000);
    }

    public static void removeClient(int clientId) {
        activeClients.remove(clientId);
        System.out.println("Client " + clientId + " disconnected");
    }
}