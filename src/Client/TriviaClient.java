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

public class TriviaClient extends ClientWindow {
	// Network communication variables
	private Socket tcpSocket;
	private DatagramSocket udpSocket;
	private InetAddress serverAddress;
	private int tcpPort;
	private int udpPort;
	private BufferedReader fromServer;
	private PrintWriter toServer;

	// Client identification
	private final int clientID;
	private int currentScore = 0;
	private int currentQuestion = 1;
	private boolean canAnswer = false;
	private boolean polling = false;

	// UI elements
	private JFrame window;
	private JLabel questionLabel;
	private JLabel scoreLabel;
	private JButton pollButton;
	private JButton submitButton;
	private JRadioButton[] options;
	private ButtonGroup optionGroup;

	// Timer variables
	private TimerTask clock;
	private JLabel timerLabel;

	// Constants
	private static final int POLL_TIMER = 15;
	private static final int ANSWER_TIMER = 10;

	/**
	 * Constructor sets up the client and establishes connection with the server
	 */
	public TriviaClient(String host, int tcpPort, int udpPort, int clientID) {
		super(); // Initialize the GUI using the parent class constructor

		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.clientID = clientID;

		try {
			// Connect to server
			serverAddress = InetAddress.getByName(host);

			// Set up TCP connection
			tcpSocket = new Socket(serverAddress, tcpPort);
			fromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
			toServer = new PrintWriter(tcpSocket.getOutputStream(), true);

			// Set up UDP socket
			udpSocket = new DatagramSocket();

			// Send client ID to server for identification
			toServer.println("JOIN:" + clientID);

			// Update GUI
			setScore(0);

			// Set window closing behavior
			getWindow().addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					disconnectFromServer();
					System.exit(0);
				}
			});

			// Start a thread to listen for server messages
			new Thread(this::listenForServerMessages).start();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(getWindow(), "Could not connect to server: " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Listen for incoming messages from the server via TCP
	 */
	private void listenForServerMessages() {
		try {
			String message;
			while ((message = fromServer.readLine()) != null) {
				final String finalMessage = message;
				SwingUtilities.invokeLater(() -> processServerMessage(finalMessage));
			}
		} catch (IOException e) {
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
	private void processServerMessage(String message) {
		if (message.startsWith("QUESTION:")) {
			// New question received
			handleNewQuestion(message.substring(9));
		} else if (message.equals("ACK")) {
			// Client can answer the question
			handleAckMessage();
		} else if (message.equals("NEGATIVE-ACK")) {
			// Client cannot answer, someone else was faster
			handleNegativeAckMessage();
		} else if (message.equals("CORRECT")) {
			// Answer was correct
			currentScore += 10;
			setScore(currentScore);
			JOptionPane.showMessageDialog(getWindow(), "Correct! +10 points");
		} else if (message.equals("WRONG")) {
			// Answer was wrong
			currentScore -= 10;
			setScore(currentScore);
			JOptionPane.showMessageDialog(getWindow(), "Wrong! -10 points");
		} else if (message.equals("NEXT")) {
			// Moving to next question without anyone answering
			JOptionPane.showMessageDialog(getWindow(), "No one answered. Moving to next question.");
		} else if (message.equals("KILL")) {
			// Server wants to terminate this client
			JOptionPane.showMessageDialog(getWindow(), "Server has terminated your connection.");
			System.exit(0);
		} else if (message.startsWith("GAME_OVER:")) {
			// Game has ended
			JOptionPane.showMessageDialog(getWindow(), "Game Over! " + message.substring(10));
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
	private void handleNewQuestion(String questionData) {
		String[] parts = questionData.split("\\|");
		currentQuestion = Integer.parseInt(parts[0]);

		// Set question text
		setQuestion("Q" + currentQuestion + ". " + parts[1]);

		// Set options
		for (int i = 0; i < 4; i++) {
			setOption(i, parts[i + 2]);
		}

		// Reset UI state for new question
		resetUIForNewQuestion();

		// Start polling phase
		startPollingPhase();
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
	 * Start the polling phase with 15-second timer
	 */
	private void startPollingPhase() {
		// Start the 15-second poll timer
		startTimer(POLL_TIMER);
	}

	/**
	 * Start the answer phase with 10-second timer
	 */
	private void startAnswerPhase() {
		// Enable options and submit button
		for (int i = 0; i < 4; i++) {
			getOption(i).setEnabled(true);
		}
		getSubmitButton().setEnabled(true);

		// Start the 10-second answer timer
		startTimer(ANSWER_TIMER);
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
								// Penalize for not answering in time
								currentScore -= 20;
								setScore(currentScore);
								JOptionPane.showMessageDialog(getWindow(), "Time's up! -20 points");
								toServer.println("TIMEOUT");
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
	 * Handle when server acknowledges this client to answer
	 */
	private void handleAckMessage() {
		polling = false;
		canAnswer = true;
		getPollButton().setEnabled(false);
		JOptionPane.showMessageDialog(getWindow(), "You were first! Go ahead and answer.");
		startAnswerPhase();
	}

	/**
	 * Handle when server denies this client to answer
	 */
	private void handleNegativeAckMessage() {
		JOptionPane.showMessageDialog(getWindow(), "Someone else beat you to it!");
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
			// Send UDP "buzz" message to server
			String buzzMessage = "BUZZ:" + clientID + ":" + currentQuestion;
			byte[] sendData = buzzMessage.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, udpPort);
			udpSocket.send(sendPacket);

			System.out.println("Sent buzz to server: " + buzzMessage);
		} catch (IOException e) {
			System.out.println("Error sending buzz: " + e.getMessage());
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
		toServer.println("ANSWER:" + selectedOption);

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
				toServer.println("LEAVE:" + clientID);
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
		getQuestionLabel().setText(text);
	}

	/**
	 * Get question label
	 */
	private JLabel getQuestionLabel() {
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
	public static void main(String[] args) {
		// Default connection parameters
		String host = "localhost";
		int tcpPort = 9000;
		int udpPort = 9001;

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

		// Start the client
		new TriviaClient(host, tcpPort, udpPort, clientID);
	}
}

/**
 * ClientWindow class to handle the GUI elements
 */
abstract class ClientWindow implements ActionListener {
	// Window components
	private JFrame window;
	private JLabel questionLabel;
	private JLabel scoreLabel;
	private JLabel timerLabel;
	private JButton pollButton;
	private JButton submitButton;
	private JRadioButton[] options;
	private ButtonGroup optionGroup;

	/**
	 * Constructor to initialize the GUI
	 */
	public ClientWindow() {
		// Set up the main window
		window = new JFrame("Trivia Game Client");
		window.setSize(500, 400);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());

		// Create the question panel
		JPanel questionPanel = new JPanel(new BorderLayout());
		questionLabel = new JLabel("Waiting for question...");
		questionLabel.setFont(new Font("Arial", Font.BOLD, 14));
		questionPanel.add(questionLabel, BorderLayout.CENTER);
		window.add(questionPanel, BorderLayout.NORTH);

		// Create options panel
		JPanel optionsPanel = new JPanel(new GridLayout(4, 1));
		options = new JRadioButton[4];
		optionGroup = new ButtonGroup();

		for (int i = 0; i < 4; i++) {
			options[i] = new JRadioButton("Option " + (i + 1));
			options[i].setActionCommand("Option " + (i + 1));
			options[i].addActionListener(this);
			options[i].setEnabled(false);
			optionGroup.add(options[i]);
			optionsPanel.add(options[i]);
		}

		window.add(optionsPanel, BorderLayout.CENTER);

		// Create bottom panel for buttons and score
		JPanel bottomPanel = new JPanel(new BorderLayout());

		// Score panel
		JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		scoreLabel = new JLabel("Score: 0");
		scorePanel.add(scoreLabel);

		// Timer panel
		JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		timerLabel = new JLabel("Time: --");
		timerPanel.add(timerLabel);

		// Buttons panel
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pollButton = new JButton("Poll");
		pollButton.setActionCommand("Poll");
		pollButton.addActionListener(this);

		submitButton = new JButton("Submit");
		submitButton.setActionCommand("Submit");
		submitButton.addActionListener(this);
		submitButton.setEnabled(false);

		buttonsPanel.add(pollButton);
		buttonsPanel.add(submitButton);

		// Add panels to bottom panel
		bottomPanel.add(scorePanel, BorderLayout.WEST);
		bottomPanel.add(timerPanel, BorderLayout.CENTER);
		bottomPanel.add(buttonsPanel, BorderLayout.EAST);

		window.add(bottomPanel, BorderLayout.SOUTH);

		// Display the window
		window.setVisible(true);
	}

	/**
	 * Abstract method to be implemented by subclasses
	 */
	@Override
	public abstract void actionPerformed(ActionEvent e);
}