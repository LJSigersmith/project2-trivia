package Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

// Import the common package classes used by the server
import common.Message;
import common.Player;
import common.Question;

public class TriviaClient extends ClientWindow {
	// Network communication variables
	private Socket tcpSocket;
	private DatagramSocket udpSocket;
	private InetAddress serverAddress;
	private int tcpPort;
	private int udpPort;
	private ObjectInputStream fromServer;
	private ObjectOutputStream toServer;

	// Client identification
	private final int clientID;
	private int currentScore = 0;
	private int currentQuestion = 1;
	private boolean canAnswer = false;
	private boolean polling = false;

	// Timer variables
	private TimerTask clock;

	// Constants
	private static final int POLL_TIMER = 15;
	private static final int ANSWER_TIMER = 10;

	/**
	 * Constructor sets up the client and establishes connection with the server
	 */
	public TriviaClient(String host, int tcpPort, int udpPort) {
		super(); // Initialize the GUI using the parent class constructor

		// Get client ID from user
		String clientIDStr = JOptionPane.showInputDialog("Enter your client ID (1-10):");
		int clientID = 1;
		try {
			clientID = Integer.parseInt(clientIDStr);
			if (clientID < 1 || clientID > 10) {
				JOptionPane.showMessageDialog(null, "Invalid client ID. Using default ID = 1");
				clientID = 1;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Invalid client ID. Using default ID = 1");
		}

		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.clientID = clientID;


		System.out.println("Attempting to connect to server...");
		try {
			// Connect to server
			serverAddress = InetAddress.getByName(host);

			// Set up TCP connection
			tcpSocket = new Socket(serverAddress, tcpPort);
			System.out.println("TCP connection established");

			// Order is important: output stream must be created before input stream
			try {
				toServer = new ObjectOutputStream(tcpSocket.getOutputStream());
				toServer.flush(); // Ensure the stream is flushed
				System.out.println("ObjectOutputStream created and flushed");
			
			} catch (IOException e) {
				System.out.println("Error creating Object Streams: " + e.getMessage()); // Log any issues
				JOptionPane.showMessageDialog(getWindow(), "Error creating streams: " + e.getMessage());
				System.exit(1);
			}

			// Set up UDP socket
			udpSocket = new DatagramSocket();
			System.out.println("UDP socket created");

			// Set window closing behavior
			//getWindow().addWindowListener(new WindowAdapter() {
			//	@Override
			//	public void windowClosing(WindowEvent e) {
			//		disconnectFromServer();
			//		System.exit(0);
			//	}
			//});

			// Start a thread to listen for server messages
			new Thread(this::listenForServerMessages).start();

		} catch (IOException e) {
			System.out.println("Error in TriviaClient constructor: " + e.getMessage());  // Log exception
			JOptionPane.showMessageDialog(getWindow(), "Could not connect to server: " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Send a JOIN message to the server via TCP
	 */
	private void sendJoinMessage() {

			Message joinMessage = new Message();
			joinMessage.setType(Message.MSG_JOIN_GAME_REQUEST);
			joinMessage.setNodeID(clientID);
			joinMessage.setTimestamp(System.currentTimeMillis());
			joinMessage.setData(null);

			sendMessageToServer(joinMessage);
			System.out.println("Sent JOIN message to server with ID: " + clientID);
	}

	/**
	 * Send a message to the server via TCP
	 */
	private void sendMessageToServer(Message message) {
		
		try {
			if (toServer == null) {
				toServer = new ObjectOutputStream(tcpSocket.getOutputStream());
				toServer.flush();
			}
		
			toServer.writeObject(message);
			toServer.flush();

		} catch (IOException e) { System.out.println("Error: " + e.getMessage());}

	}

	/**
	 * Listen for incoming messages from the server via TCP
	 */
	private void listenForServerMessages() {
		try {
			fromServer = new ObjectInputStream(tcpSocket.getInputStream());
			System.out.println("ObjectInputStream created");
			while (true) {
				Object obj = fromServer.readObject();
				if (obj instanceof Message) {
					final Message message = (Message) obj;
					SwingUtilities.invokeLater(() -> processServerMessage(message));
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Server connection lost: " + e.getMessage());
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(getWindow(), "Server connection lost");
				System.exit(1);
			});
		}
	}

	/**
	 * Process messages received from the server
	 */
	private void processServerMessage(Message message) {
		switch (message.getType()) {
			case Message.MSG_ACKNOWLEDGE_JOIN_REQUEST:
				System.out.println("Server acknowledged our join request");
				break;

			case Message.MSG_STARTING_GAME:
				System.out.println("Game is starting");
				JOptionPane.showMessageDialog(getWindow(), "Game is starting!");
				break;

			case Message.MSG_QUESTION:
				// New question received
				System.out.println("Question recieved");
				handleNewQuestion(message);
				break;

			case Message.MSG_GOOD_TO_ANSWER:
				// Client can answer the question
				handleGoodToAnswerMessage(message);
				break;

			case Message.MSG_SCORE:
				// Process score update
				handleScoreMessage(message);
				break;

			case Message.MSG_GAME_OVER:
				// Game has ended
				JOptionPane.showMessageDialog(getWindow(), "Game Over!");
				break;

			default:
				System.out.println("Received unknown message type: " + message.getType());
		}
	}

	/**
	 * Get the main window frame
	 */
	private JFrame getWindow() {
		return window;
	}

	/**
	 * Handle when a new question is received from server
	 */
	private void handleNewQuestion(Message message) {
		try {
			// Deserialize the Question object from the message data
			ByteArrayInputStream bis = new ByteArrayInputStream(message.getData());
			ObjectInputStream ois = new ObjectInputStream(bis);
			Question question = (Question) ois.readObject();
			ois.close();

			currentQuestion++;

			// Set question text
			System.out.println("Deseriaized Question");
			System.out.println(question);
			setQuestion("Q" + currentQuestion + ". " + question.getQuestion());

			// Set options
			String[] options = question.getOptions();
			for (int i = 0; i < options.length; i++) {
				setOption(i, options[i]);
			}

			// Reset UI state for new question
			resetUIForNewQuestion();

			// Start polling phase - use the timestamp from message as expiration time
			startPollingPhase((int)((message.getTimestamp() - System.currentTimeMillis()) / 1000));

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error processing question message: " + e.getMessage());
		}
	}

	/**
	 * Handle when server says this client can answer
	 */
	private void handleGoodToAnswerMessage(Message message) {
		polling = false;
		canAnswer = true;
		getPollButton().setEnabled(false);
		JOptionPane.showMessageDialog(getWindow(), "You were first! Go ahead and answer.");

		// Start answer phase with time limit from message timestamp
		int timeLeft = (int)((message.getTimestamp() - System.currentTimeMillis()) / 1000);
		startAnswerPhase(Math.max(1, timeLeft)); // Ensure at least 1 second
	}

	/**
	 * Handle score update from server
	 */
	private void handleScoreMessage(Message message) {
		try {
			int scoreChange = Integer.parseInt(new String(message.getData()));
			currentScore += scoreChange;
			setScore(currentScore);

			if (scoreChange > 0) {
				JOptionPane.showMessageDialog(getWindow(), "Correct! +" + scoreChange + " points");
			} else if (scoreChange < 0 && scoreChange > -20) {
				JOptionPane.showMessageDialog(getWindow(), "Wrong! " + scoreChange + " points");
			} else {
				JOptionPane.showMessageDialog(getWindow(), "Didn't Answer In Time! " + scoreChange + " points");
			}

			// Reset UI for next question
			canAnswer = false;

		} catch (NumberFormatException e) {
			System.out.println("Error parsing score: " + e.getMessage());
		}
	}

	/**
	 * Reset UI elements for a new question
	 */
	private void resetUIForNewQuestion() {
		// Enable poll button, disable submit and options
		getPollButton().setEnabled(true);
		getSubmitButton().setEnabled(false);

		for (int i = 0; i < 4; i++) {
			getOption(i).setEnabled(false);
		}

		// Clear any selected option
		getOptionGroup().clearSelection();

		// Reset state variables
		canAnswer = false;
		polling = true;
	}

	/**
	 * Get poll button
	 */
	private JButton getPollButton() {
		return pollButton;
	}

	/**
	 * Get submit button
	 */
	private JButton getSubmitButton() {
		return submitButton;
	}

	/**
	 * Get option radio button by index
	 */
	private JRadioButton getOption(int i) {
		return options[i];
	}

	/**
	 * Get option button group
	 */
	private ButtonGroup getOptionGroup() {
		return optionGroup;
	}

	/**
	 * Start the polling phase with timer
	 */
	private void startPollingPhase(int seconds) {
		// Start the poll timer
		startTimer(seconds > 0 ? seconds : POLL_TIMER);
	}

	/**
	 * Start the answer phase with timer
	 */
	private void startAnswerPhase(int seconds) {
		// Enable options and submit button
		for (int i = 0; i < 4; i++) {
			getOption(i).setEnabled(true);
		}
		getSubmitButton().setEnabled(true);

		// Start the answer timer
		startTimer(seconds > 0 ? seconds : ANSWER_TIMER);
	}

	/**
	 * Start a timer with the specified duration
	 */
	private void startTimer(int duration) {
		// Cancel any existing timer
		TimerTask currentClock = getClock();
		if (currentClock != null) {
			currentClock.cancel();
		}

		// Create a new timer task
		final int[] remainingTime = {duration};
		TimerTask newClock = new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(() -> {
					remainingTime[0]--;
					timerLabel.setText("Time: " + remainingTime[0]);

					if (remainingTime[0] <= 0) {
						this.cancel();
						if (polling) {
							// Polling time ended
							polling = false;
							getPollButton().setEnabled(false);

							// If this client can answer, they missed their chance
							if (canAnswer && getOptionGroup().getSelection() == null) {
								// Timeout message
								sendTimeoutMessage();
							}
						}
					}
				});
			}
		};

		// Set the new clock and start it
		setClock(newClock);
		Timer t = new Timer();
		t.schedule(newClock, 0, 1000);

		// Initialize timer display
		timerLabel.setText("Time: " + duration);
	}

	/**
	 * Send timeout message to server
	 */
	private void sendTimeoutMessage() {
		Message timeoutMsg = new Message();
		timeoutMsg.setType(Message.MSG_TIMEOUT);
		timeoutMsg.setNodeID(clientID);
		timeoutMsg.setTimestamp(System.currentTimeMillis());
		sendMessageToServer(timeoutMsg);
	}

	/**
	 * Set the current clock
	 */
	private void setClock(TimerTask newClock) {
		this.clock = newClock;
	}

	/**
	 * Get the current clock
	 */
	private TimerTask getClock() {
		return clock;
	}

	/**
	 * Handle user actions (buttons and radio buttons)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();

		// Handle different actions based on the button pressed
		switch (input) {
			case "Poll":
				handlePollButton();
				break;
			case "Submit":
				handleSubmitButton();
				break;
			case "Option 1":
			case "Option 2":
			case "Option 3":
			case "Option 4":
				// Option selected - no immediate action needed
				break;
			default:
				System.out.println("Incorrect Option");
		}
	}

	/**
	 * Handle when Poll button is clicked
	 */
	private void handlePollButton() {
		if (!polling) return;

		try {
			// Create a poll message compatible with the server
			Message pollMessage = new Message();
			pollMessage.setType(Message.MSG_POLL);
			pollMessage.setNodeID(clientID);
			pollMessage.setTimestamp(System.currentTimeMillis());
			pollMessage.setData(Integer.toString(currentQuestion).getBytes());

			// Serialize the message
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteStream);
			out.writeObject(pollMessage);
			out.flush();

			// Send UDP "poll" message to server
			byte[] sendData = byteStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, udpPort);
			udpSocket.send(sendPacket);

			System.out.println("Sent poll to server for question " + currentQuestion);
		} catch (IOException e) {
			System.out.println("Error sending poll: " + e.getMessage());
		}
	}

	/**
	 * Handle when Submit button is clicked
	 */
	private void handleSubmitButton() {
		if (!canAnswer) return;

		// Find which option was selected
		int selectedOption = -1;
		for (int i = 0; i < 4; i++) {
			if (getOption(i).isSelected()) {
				selectedOption = i + 1;
				break;
			}
		}

		if (selectedOption == -1) {
			JOptionPane.showMessageDialog(getWindow(), "Please select an answer");
			return;
		}

		// Send answer to server
		Message answerMessage = new Message();
		answerMessage.setType(Message.MSG_ANSWER);
		answerMessage.setNodeID(clientID);
		answerMessage.setTimestamp(System.currentTimeMillis());
		answerMessage.setData(Integer.toString(selectedOption).getBytes());
		sendMessageToServer(answerMessage);

		// Disable submit button and options after submission
		getSubmitButton().setEnabled(false);
		for (int i = 0; i < 4; i++) {
			getOption(i).setEnabled(false);
		}

		canAnswer = false;
	}

	/**
	 * Close connections when exiting
	 */
	private void disconnectFromServer() {
		try {
			if (toServer != null) {
				Message leaveMessage = new Message();
				leaveMessage.setType(Message.MSG_LEAVE);
				leaveMessage.setNodeID(clientID);
				leaveMessage.setTimestamp(System.currentTimeMillis());
				sendMessageToServer(leaveMessage);

				toServer.close();
			}
			if (fromServer != null) {
				fromServer.close();
			}
			if (tcpSocket != null) {
				tcpSocket.close();
			}
			if (udpSocket != null) {
				udpSocket.close();
			}
		} catch (IOException e) {
			System.out.println("Error while disconnecting: " + e.getMessage());
		}
	}

	/**
	 * Update score display
	 */
	private void setScore(int score) {
		getScoreLabel().setText("Score: " + score);
	}

	/**
	 * Get score label
	 */
	private JLabel getScoreLabel() {
		return scoreLabel;
	}

	/**
	 * Update question display
	 */
	private void setQuestion(String text) {
		System.out.println("Setting q text: " + text);
		getQuestionLabel().setText(text);
	}

	/**
	 * Get question label
	 */
	private JLabel getQuestionLabel() {
		if (questionLabel == null) {
			questionLabel = new JLabel("Question will appear here");
		}
		return questionLabel;
	}

	/**
	 * Update option text
	 */
	private void setOption(int index, String text) {
		if (index >= 0 && index < 4) {
			getOption(index).setText(text);
		}
	}

	/**
	 * Main method to start the client
	 */
	public void start() {

		System.out.println("Sending JOIN");
		// Send JOIN message to server
		sendJoinMessage();

		// Update GUI
		//setScore(0);
	}
}