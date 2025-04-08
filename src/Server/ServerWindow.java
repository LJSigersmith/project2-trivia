package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

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
        questionPanel.setBorder(BorderFactory.createTitledBorder("Current Question"));
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
        leftPanel.add(pollingQueuePanel);

        // Panel for Connected Players
        JPanel connectedPlayersPanel = new JPanel();
        connectedPlayersPanel.setLayout(new BorderLayout());
        connectedPlayersList = new JList<>(new DefaultListModel<>());
        connectedPlayersPanel.add(new JScrollPane(connectedPlayersList), BorderLayout.CENTER);
        connectedPlayersPanel.setBorder(BorderFactory.createTitledBorder("Connected Players"));
        rightPanel.add(connectedPlayersPanel);

        // Panel for Client Info
        JPanel clientInfoPanel = new JPanel();
        clientInfoPanel.setLayout(new GridLayout(3, 1, 5, 5));  // 3 rows for each label
        clientInfoPanel.setBorder(BorderFactory.createTitledBorder("Client Info"));
        
        clientAnsweredLabel = new JLabel("Client Answered: ");
        clientInfoPanel.add(clientAnsweredLabel);

        clientAnswerLabel = new JLabel("Client Answer: ");
        clientInfoPanel.add(clientAnswerLabel);

        clientAnsweringLabel = new JLabel("Client Answering: ");
        clientInfoPanel.add(clientAnsweringLabel);
        
        rightPanel.add(clientInfoPanel);

        // Add left and right panels to the main panel
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Log Text Area at the bottom
        JTextArea logArea = new JTextArea(5, 40);  // 5 rows, 40 columns
        logArea.setEditable(false);  // Make the log area non-editable
        JScrollPane logScrollPane = new JScrollPane(logArea);  // Add scroll functionality
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));
        mainPanel.add(logScrollPane, BorderLayout.SOUTH);

        // Set the frame to be visible
        frame.setVisible(true);
    }

    protected void updateQuestionLabel(Question question) {

        StringBuilder questionText = new StringBuilder("<html>");
        questionText.append("Current Question:<br/>");
        questionText.append(question.getQuestion()).append("<br/>");
        String[] options = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            questionText.append(i).append(". ").append(options[i]).append("<br/>");
        }
        questionText.append("</html>");
        currentQuestionLabel.setText(questionText.toString());

    }
    protected void addToPollingQueueLabel(Player player) {
        DefaultListModel<String> model = (DefaultListModel<String>) pollingQueueList.getModel();
        model.addElement(player.toString());
        pollingQueueList.setModel(model);
    }
    protected void clearPollingQueueLabel() {
        pollingQueueList.setModel(new DefaultListModel<>());
    }
    protected void addToConnectedPlayersList(Player player) {
        DefaultListModel<String> model = (DefaultListModel<String>) connectedPlayersList.getModel();
        model.addElement(player.toString());
        connectedPlayersList.setModel(model);
    }
    protected void clearConnectedPlayersList() {
        connectedPlayersList.setModel(new DefaultListModel<>());
    }
    protected void updateClientAnsweredLabel(Boolean update) {
        if (update) {
            clientAnsweredLabel.setText("TRUE");
        } else {
            clientAnswerLabel.setText("FALSE");
        }
    }
    protected void updateClientAnsweringLabel(Player player) {
        clientAnsweringLabel.setText(player.toString());
    }
    protected void updateClientAnswerLabel(String ans) {
        clientAnswerLabel.setText(ans);
    }
}
