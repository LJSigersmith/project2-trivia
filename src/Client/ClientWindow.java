package Client;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.*;

import common.Question;
import common.Message;

public class ClientWindow implements ActionListener
{
	private JButton poll;
	private JButton submit;
	private JRadioButton options[];
	private ButtonGroup optionGroup;
	private JLabel question;
	private JLabel timer;
	private JLabel score;
	private TimerTask clock;
	
	private JFrame window;
	
	private static SecureRandom random = new SecureRandom();

	private Question _currentQuestion;

	private int _clientID;
	private int _serverPort;
	private InetAddress _serverAddress;
	
	void setupGUI() {

		window = new JFrame("Trivia");
		question = new JLabel("Q1. This is a sample question"); // represents the question
		window.add(question);
		question.setBounds(10, 5, 350, 100);;
		
		options = new JRadioButton[4];
		optionGroup = new ButtonGroup();
		for(int index=0; index<options.length; index++)
		{
			options[index] = new JRadioButton("Option " + (index+1));  // represents an option
			// if a radio button is clicked, the event would be thrown to this class to handle
			options[index].addActionListener(this);
			options[index].setBounds(10, 110+(index*20), 350, 20);
			window.add(options[index]);
			optionGroup.add(options[index]);
		}

		timer = new JLabel("TIMER");  // represents the countdown shown on the window
		timer.setBounds(250, 250, 100, 20);
		clock = new TimerCode(30);  // represents clocked task that should run after X seconds
		Timer t = new Timer();  // event generator
		t.schedule(clock, 0, 1000); // clock is called every second
		window.add(timer);
		
		
		score = new JLabel("SCORE"); // represents the score
		score.setBounds(50, 250, 100, 20);
		window.add(score);

		poll = new JButton("Poll");  // button that use clicks/ like a buzzer
		poll.setBounds(10, 300, 100, 20);
		poll.addActionListener(this);  // calls actionPerformed of this class
		window.add(poll);
		
		submit = new JButton("Submit");  // button to submit their answer
		submit.setBounds(200, 300, 100, 20);
		submit.addActionListener(this);  // calls actionPerformed of this class
		window.add(submit);
		
		
		window.setSize(400,400);
		window.setBounds(50, 50, 400, 400);
		window.setLayout(null);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		
	}
	void displayQuestion(Question q) {

		question.setText(q.getQuestion());

		String[] optionsArray = q.getOptions();
		for (int i = 0; i < options.length; i++) {
			options[i].setText(optionsArray[i]);
		}

	}
	
	void handlePollSelected() {

		// Send poll to server
	}
	void handleSubmitSelected() {

		// Send selected option to server
	}
	void handleOptionSelected(String option) {

		// Set selected option
	}

	private void _sendMessageToServer(Message message) {
		
		try {

		DatagramSocket socket = new DatagramSocket();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		
		oos.writeObject(message);
		oos.flush();
		byte[] data = os.toByteArray();

		DatagramPacket payload = new DatagramPacket(data, data.length, _serverAddress, _serverPort);
		socket.send(payload);
		socket.close();

		} catch (Exception e) {
			System.out.println("Error sending message to server");
			e.printStackTrace();
		}
		
	}

	public ClientWindow()
	{
		//JOptionPane.showMessageDialog(window, "This is a trivia game");
		_clientID = random.nextInt(1000);

		// Setup GUI
		setupGUI();

		// Setup connection to server
		_serverPort = 5001;
		try {
			_serverAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Error setting server address");
			e.printStackTrace();
		}
		//_serverAddress = InetAddress.getByName("127.0.0.1");

		// Send request to server to join game
		Message joinGameRequest = new Message();
		joinGameRequest.setType(Message.MSG_JOIN_GAME_REQUEST);
		joinGameRequest.setNodeID(_clientID);
		joinGameRequest.setTimestamp(System.currentTimeMillis());
		joinGameRequest.setData(null);
		_sendMessageToServer(joinGameRequest);

		// Wait for server to send starting game message

		// Wait for server to send question

		// Display question


	}

	// Handle Buttons
	@Override
	public void actionPerformed(ActionEvent e)
	{
		System.out.println("You clicked " + e.getActionCommand());
		
		// input refers to the radio button you selected or button you clicked
		String input = e.getActionCommand();  
		switch(input)
		{
			case "Option 1":
				handleOptionSelected(_currentQuestion.getOptions()[0]);
				break;
			case "Option 2":
				handleOptionSelected(_currentQuestion.getOptions()[1]);
				break;
			case "Option 3":
				handleOptionSelected(_currentQuestion.getOptions()[2]);
				break;
			case "Option 4":
				handleOptionSelected(_currentQuestion.getOptions()[3]);
				break;
			case "Poll":	
				handlePollSelected();
				break;
			case "Submit":	
				handleSubmitSelected();
				break;
			default:
				System.out.println("Incorrect Option");
		}
		
	}
	
	// Timer
	public class TimerCode extends TimerTask
	{
		private int duration;  // write setters and getters as you need
		public TimerCode(int duration)
		{
			this.duration = duration;
		}
		@Override
		public void run()
		{
			if(duration < 0)
			{
				timer.setText("Timer expired");
				window.repaint();
				this.cancel();  // cancel the timed task
				return;
				// you can enable/disable your buttons for poll/submit here as needed
			}
			
			if(duration < 6)
				timer.setForeground(Color.red);
			else
				timer.setForeground(Color.black);
			
			timer.setText(duration+"");
			duration--;
			window.repaint();
		}
	}
	
}