package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; /**
 * ClientWindow class to handle the GUI elements
 */
public abstract class ClientWindow implements ActionListener {
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
