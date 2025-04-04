package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UDPThread implements Runnable {
    private DatagramSocket socket;
    private final int udpPort;
    private volatile boolean running = true;

    // Store client UDP endpoints - clientId -> [InetAddress, port]
    private final Map<Integer, ClientUDPInfo> clientEndpoints = new ConcurrentHashMap<>();

    // Constructor
    public UDPThread(int udpPort) {
        this.udpPort = udpPort;
        try {
            this.socket = new DatagramSocket(udpPort);
            System.out.println("UDP server started on port " + udpPort);
        } catch (SocketException e) {
            System.err.println("Failed to create UDP socket: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];

        while (running && socket != null && !socket.isClosed()) {
            try {
                // Create packet for receiving data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Block until a packet is received
                socket.receive(packet);

                // Process received packet
                String message = new String(packet.getData(), 0, packet.getLength());
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                System.out.println("UDP received: " + message + " from " + clientAddress + ":" + clientPort);

                // Process the message
                processUDPMessage(message, clientAddress, clientPort);

            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.err.println("UDP error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Process incoming UDP messages
     */
    private void processUDPMessage(String message, InetAddress address, int port) {
        if (message == null || message.isEmpty()) {
            return;
        }

        String[] parts = message.split("\\|");
        if (parts.length < 2) {
            return;
        }

        String command = parts[0];

        try {
            switch (command) {
                case "REGISTER":
                    // Register client endpoint for UDP communication
                    // Format: REGISTER|clientId
                    int clientId = Integer.parseInt(parts[1]);
                    registerClient(clientId, address, port);
                    break;

                case "BROADCAST":
                    // Broadcast message to all clients
                    // Format: BROADCAST|clientId|message
                    if (parts.length >= 3) {
                        int senderId = Integer.parseInt(parts[1]);
                        String broadcastMessage = parts[2];
                        broadcastToAll(senderId, broadcastMessage);
                    }
                    break;

                case "POSITION":
                    // Update position data (for games)
                    // Format: POSITION|clientId|x|y|z
                    if (parts.length >= 5) {
                        int posClientId = Integer.parseInt(parts[1]);
                        String positionData = "POS|" + parts[1] + "|" + parts[2] + "|" + parts[3] + "|" + parts[4];
                        broadcastToAll(posClientId, positionData);
                    }
                    break;

                default:
                    System.out.println("Unknown UDP command: " + command);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid client ID in UDP message: " + e.getMessage());
        }
    }

    /**
     * Register a client's UDP endpoint
     */
    public void registerClient(int clientId, InetAddress address, int port) {
        clientEndpoints.put(clientId, new ClientUDPInfo(address, port));
        System.out.println("Registered UDP endpoint for client " + clientId + ": " + address + ":" + port);

        // Send confirmation
        sendToClient(clientId, "UDP_REGISTERED");
    }

    /**
     * Remove a client
     */
    public void removeClient(int clientId) {
        clientEndpoints.remove(clientId);
        System.out.println("Removed UDP endpoint for client " + clientId);
    }

    /**
     * Send UDP message to a specific client
     */
    public void sendToClient(int clientId, String message) {
        ClientUDPInfo clientInfo = clientEndpoints.get(clientId);
        if (clientInfo == null) {
            System.out.println("Cannot send UDP to client " + clientId + ": not registered");
            return;
        }

        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    clientInfo.address,
                    clientInfo.port
            );
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error sending UDP to client " + clientId + ": " + e.getMessage());
        }
    }

    /**
     * Broadcast message to all registered clients except sender
     */
    public void broadcastToAll(int senderId, String message) {
        for (Map.Entry<Integer, ClientUDPInfo> entry : clientEndpoints.entrySet()) {
            int clientId = entry.getKey();

            // Skip the sender
            if (clientId == senderId) {
                continue;
            }

            ClientUDPInfo clientInfo = entry.getValue();
            try {
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        clientInfo.address,
                        clientInfo.port
                );
                socket.send(packet);
            } catch (IOException e) {
                System.err.println("Error broadcasting to client " + clientId + ": " + e.getMessage());
            }
        }
    }

    /**
     * Stop the UDP thread
     */
    public void shutdown() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("UDP server shut down");
        }
    }

    /**
     * Helper class to store client UDP information
     */
    private static class ClientUDPInfo {
        final InetAddress address;
        final int port;

        ClientUDPInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }
    }
}