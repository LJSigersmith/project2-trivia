import Client.ClientWindow;
import Client.NetworkHandler;
import Client.TriviaClient;
import Server.Server;

import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main
{
	public static void main(String[] args)
	{

		if (args[0].equals("SERVER")) {
			Server server = new Server();
			server.start();
		} else if (args[0].equals("CLIENT")) {
			ClientWindow window = new ClientWindow() {
				@Override
				public void actionPerformed(ActionEvent e) {

				}
			};

			String serverAddress = "";
			try {
			serverAddress = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				System.out.println(e.getMessage());
			}
			int tcpPort = 5001;
			int udpPort = 5002;

			NetworkHandler networkHandler = new NetworkHandler(
				serverAddress, tcpPort, udpPort, message -> System.out.println("Server: " + message)
			);

			try {
				networkHandler.connect();
				System.out.println("Connected to server. Client ID: " + networkHandler.getClientId());
			} catch (Exception e) {
				System.err.println("Client error: " + e.getMessage());
			} finally {
				networkHandler.disconnect();
			}
			
		} else {
			System.out.println("Invalid argument");
		}
	}
}