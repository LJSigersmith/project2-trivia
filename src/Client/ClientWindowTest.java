package Client;

import java.awt.event.ActionEvent;

public class ClientWindowTest {
    public static void main(String[] args) {
        // Launch a test client
        // For actual gameplay, use TriviaClient.main() instead
        new ClientWindow() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };
    }
}