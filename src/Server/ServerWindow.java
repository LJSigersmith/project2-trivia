package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import common.Question;
import common.Player;

public abstract class ServerWindow implements ActionListener {

    protected JFrame frame;
    protected JLabel currentQuestionLabel;
    protected JList<String> pollingQueueList;
    protected JList<String> connectedPlayersList;
    protected JLabel clientAnsweredLabel;
    protected JLabel clientAnsweringLabel;
    protected JLabel clientAnswerLabel;
    protected JLabel correctOptionLabel;
    protected JLabel gameStatusLabel;
    protected JList<String> playerScoresList;

    public ServerWindow() {

        frame = new JFrame("Trivia Game Server");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));  // Add gaps for better spacing
        frame.add(mainPanel);

        // Create the panel for the current question and options
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BorderLayout());
        currentQuestionLabel = new JLabel("<html>Current Question:<br/>A. Option A<br/>B. Option B<br/>C. Option C</html>");
        questionPanel.add(currentQuestionLabel, BorderLayout.CENTER);

        // Add label for the correct option
        correctOptionLabel = new JLabel("Correct Option: ");
        correctOptionLabel.setForeground(new Color(0, 128, 0)); // Set text color to darker green
        questionPanel.add(correctOptionLabel, BorderLayout.SOUTH);

        questionPanel.setBorder(BorderFactory.createTitledBorder("Current Question"));
        questionPanel.setPreferredSize(new Dimension(800, 100));
        mainPanel.add(questionPanel, BorderLayout.NORTH);

        // Create a split panel to hold the left and right side content
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(2, 1, 10, 10));  // Top and bottom panels
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(3, 1, 10, 10));  // 3 sections for connected players and client info

        // Panel for Polling Queue
        JPanel pollingQueuePanel = new JPanel();
        pollingQueuePanel.setLayout(new BorderLayout());
        pollingQueueList = new JList<>(new DefaultListModel<>());
        pollingQueuePanel.add(new JScrollPane(pollingQueueList), BorderLayout.CENTER);
        pollingQueuePanel.setBorder(BorderFactory.createTitledBorder("Polling Queue"));
        pollingQueuePanel.setPreferredSize(new Dimension(200, 200));
        leftPanel.add(pollingQueuePanel);

        // Panel for Connected Players
        JPanel connectedPlayersPanel = new JPanel();
        connectedPlayersPanel.setLayout(new BorderLayout());
        connectedPlayersList = new JList<>(new DefaultListModel<>());
        connectedPlayersPanel.add(new JScrollPane(connectedPlayersList), BorderLayout.CENTER);
        connectedPlayersPanel.setBorder(BorderFactory.createTitledBorder("Connected Players"));
        connectedPlayersPanel.setPreferredSize(new Dimension(200, 200));
        rightPanel.add(connectedPlayersPanel);

        // Panel for Client Info
        JPanel clientInfoPanel = new JPanel();
        clientInfoPanel.setLayout(new GridLayout(3, 1, 5, 5));  // 3 rows for each label
        clientInfoPanel.setBorder(BorderFactory.createTitledBorder("Client Info"));
        
        clientAnsweredLabel = new JLabel("Player Answered: ");
        clientInfoPanel.add(clientAnsweredLabel);

        clientAnswerLabel = new JLabel("Player Answer: ");
        clientInfoPanel.add(clientAnswerLabel);

        clientAnsweringLabel = new JLabel("Player Answering: ");
        clientInfoPanel.add(clientAnsweringLabel);
        
        rightPanel.add(clientInfoPanel);

        // Add left and right panels to the main panel
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

        gameStatusLabel = new JLabel("Game Stage: Game Not Started");
        middlePanel.add(gameStatusLabel);

        // Panel for Player Scores
        JPanel playerScoresPanel = new JPanel();
        playerScoresPanel.setLayout(new BorderLayout());
        playerScoresList = new JList<>(new DefaultListModel<>());
        playerScoresPanel.add(new JScrollPane(playerScoresList), BorderLayout.CENTER);
        playerScoresPanel.setBorder(BorderFactory.createTitledBorder("Player Scores"));
        playerScoresPanel.setPreferredSize(new Dimension(400, 200));
        middlePanel.add(playerScoresPanel);

        mainPanel.add(middlePanel, BorderLayout.CENTER);  // Add to the center section of the layout

        // Log Text Area at the bottom
        JTextArea logArea = new JTextArea(5, 40);  // 5 rows, 40 columns
        logArea.setEditable(false);  // Make the log area non-editable
        JScrollPane logScrollPane = new JScrollPane(logArea);  // Add scroll functionality
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));
        mainPanel.add(logScrollPane, BorderLayout.SOUTH);

        // Create a custom OutputStream that appends text to JTextArea
        OutputStream outputStream = new JTextAreaOutputStream(logArea);
        PrintStream printStream = new PrintStream(outputStream);

        // Redirect System.out to the JTextArea
        System.setOut(printStream);

        // Set the frame to be visible
        frame.setVisible(true);
    }

    protected void GUI_updateQuestionLabel(Question question) {

        StringBuilder questionText = new StringBuilder("<html>");
        questionText.append("Current Question:<br/>");
        questionText.append(question.getQuestion()).append("<br/>");
        String[] options = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            questionText.append(i).append(". ").append(options[i]).append("<br/>");
        }
        questionText.append("</html>");
        currentQuestionLabel.setText(questionText.toString());
        correctOptionLabel.setText(question.getCorrectOption());

    }
    protected void GUI_updatePollingQueueLabel(ArrayList<Player> pollingQueue) {
        GUI_clearPollingQueueLabel();
        DefaultListModel<String> model = (DefaultListModel<String>) pollingQueueList.getModel();
        for (Player player : pollingQueue) {
            model.addElement(player.toString());
        }
        pollingQueueList.setModel(model);
    }
    protected void GUI_clearPollingQueueLabel() {
        pollingQueueList.setModel(new DefaultListModel<>());
    }
    protected void GUI_updateConnectedPlayersList(ArrayList<ClientHandler> connectedPlayers) {
        GUI_clearConnectedPlayersList();
        DefaultListModel<String> model = (DefaultListModel<String>) connectedPlayersList.getModel();
        for (ClientHandler client : connectedPlayers) {
            model.addElement(client.toString());
        }
        connectedPlayersList.setModel(model);
    }
    protected void GUI_clearConnectedPlayersList() {
        connectedPlayersList.setModel(new DefaultListModel<>());
    }
    protected void GUI_updateClientAnsweredLabel(Boolean update) {
        if (update) {
            clientAnsweredLabel.setText("Player Answered: TRUE");
        } else {
            clientAnsweredLabel.setText("Player Answered: FALSE");
        }
    }
    protected void GUI_updateClientAnsweringLabel(Player player) {
        if (player == null) { clientAnsweringLabel.setText("Player Answering:"); return; }
        clientAnsweringLabel.setText("Player Answering :" + player.toString());
    }
    protected void GUI_updateClientAnswerLabel(String ans) {
        clientAnswerLabel.setText("Player Answer: " + ans);
    }

    // New method to update the game status label
    protected void GUI_updateGameStatusLabel(String status) {
        gameStatusLabel.setText("Game Stage: " + status);
    }

    // New method to update the player scores list
    protected void GUI_updatePlayerScoresList(ArrayList<ClientHandler> clients) {
        DefaultListModel<String> model = (DefaultListModel<String>) playerScoresList.getModel();
        model.clear();
        for (ClientHandler client : clients) {
            model.addElement(client.toString() + " - Score: " + client.getClientScore());
        }
        playerScoresList.setModel(model);
    }

    // Custom OutputStream that writes to JTextArea
    static class JTextAreaOutputStream extends OutputStream {
        private JTextArea textArea;

        public JTextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            // Append character to JTextArea
            textArea.append(String.valueOf((char) b));
            // Automatically scroll to the end
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}
