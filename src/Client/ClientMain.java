package Client;

public class ClientMain {
    public static void main(String[] args) {

        System.out.println("Main started");

        String serverAddress = "192.152.243.162";
        int tcpPort = 5001;
        int udpPort = 5002;

        NetworkHandler networkHandler = new NetworkHandler(
                serverAddress,
                tcpPort,
                udpPort,
                message -> System.out.println("Server: " + message)
        );

        try {
            networkHandler.connect();
            System.out.println("Connected to server. Client ID: " + networkHandler.getClientId());

            networkHandler.sendUdpMessage("BUZZ|" + networkHandler.getClientId() + "|1");

            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            networkHandler.disconnect();
        }
    }
}