import Client.ClientWindow;
import Client.NetworkHandler;
import Client.TriviaClient;
import Server.Server;

import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;

public class Main
{

	public static void startServer() {
		Server server = new Server();
		server.start();
	}

	public static void startClient() {
		String serverAddress = "";
		int tcpPort = 5001;
		int udpPort = 5002;

		// Use if running client and server on same node
		try {
		serverAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		}

		//serverAddress = "10.0.0.74";

		TriviaClient client = new TriviaClient(serverAddress, tcpPort, udpPort);
		client.start();
	}
	public static void main(String[] args)
	{

		JFrame frame;
		frame = new JFrame("Trivia Game Setup");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 100);
		frame.setLayout(new java.awt.FlowLayout());

		final int[] option = new int[1];
		javax.swing.JButton serverButton = new javax.swing.JButton("Server");
		javax.swing.JButton clientButton = new javax.swing.JButton("Client");

		serverButton.addActionListener((ActionEvent e) -> {
			frame.dispose();
			option[0] = 1;
		});

		clientButton.addActionListener((ActionEvent e) -> {
			frame.dispose();
			option[0] = 2;
		});

		frame.add(serverButton);
		frame.add(clientButton);

		frame.setVisible(true);

		// Wait for option to be selected
		while (frame.isDisplayable() && option[0] == 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Thread interrupted: " + e.getMessage());
			}
		}

		if (option[0] == 1) {
			startServer();
		} else {
			startClient();
		}
	}
}