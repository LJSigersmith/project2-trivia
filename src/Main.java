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

			String serverAddress = "";
			int tcpPort = 5001;
			int udpPort = 5002;

			// Use if running client and server on same node
			//try {
			//serverAddress = InetAddress.getLocalHost().getHostAddress();
			//} catch (UnknownHostException e) {
			//	System.out.println(e.getMessage());
			//}

			serverAddress = "10.0.0.74";

			System.out.println("Starting client");
			TriviaClient client = new TriviaClient(serverAddress, tcpPort, udpPort);
			System.out.println("Starting client");
			client.start();
			
		} else {
			System.out.println("Invalid argument");
		}
	}
}