
# Trivia Game

This project is a simple **client-server** trivia game written in Java. The client and server communicate using **TCP** and **UDP** protocols, allowing multiple clients to join the game, answer questions, and track scores.

## Project Structure

The project is organized into the following directory structure:

```
/trivia-game
├── src/                    # Source files for client, server, and common classes
│   ├── Client/             # Client-side logic and GUI
│   │    ├── TriviaClient.java
│   │    ├── ClientWindow.java
│   │    ├── NetworkHandler.java
│   │    └── ...
│   ├── Server/             # Server-side logic
│   │    ├── Server.java
│   │    ├── ClientHandler.java
│   │    └── ...
│   └── Common/             # Shared classes between client and server
│        ├── Message.java
│        ├── Player.java
│        └── Question.java
├── out/                    # Compiled .class files (output folder)
│   ├── Client/             # Compiled client-side classes
│   ├── Server/             # Compiled server-side classes
│   └── Common/             # Compiled common classes
└── README.md               # Project documentation (this file)
```

- **`src/`**: Contains the source code for both the client and server components, as well as the shared classes.
- **`out/`**: The compiled `.class` files will be placed here, organized by package structure.

## Requirements

- **Java**: Version 8 or above.

## Setup

### Step 1: Clone the Repository
Clone the repository to your local machine:

```bash
git clone https://github.com/LJSigersmith/project2-trivia.git
cd project2-trivia
```

### Step 2: Compile the Code

To compile the `.java` files and generate `.class` files in the `out/` directory, use the following command:

```bash
javac -d out/ src/**/*.java
```

This will compile all `.java` files from the `src/` directory and place the resulting `.class` files into the `out/` directory, maintaining the package structure.

### Step 3: Create the JAR File

Once the `.java` files are compiled into `.class` files, you can package them into a `.jar` file. Run the following command to create the JAR:

```bash
jar cmf src/META-INF/MANIFEST.MF trivia-game.jar -C out/ .
```

This command creates a `trivia-game.jar` file, including all compiled `.class` files from the `out/` directory and its subdirectories.

### Step 4: Run the JAR File

To run the JAR file, use the following command:

```bash
java -jar trivia-game.jar
```

This will launch the trivia game, and you can either start the server or connect as a client based on the program's prompts.

## Design

# Trivia Game - Design Overview

This document provides a high-level design of the **server** and **client** components for the Trivia Game. The system consists of two main parts: the server, which manages the game state and client interactions, and the client, which interacts with the player and sends/receives data to/from the server.

## System Architecture

The system is designed using a **client-server** architecture, where:

- **Server**: Manages the game, processes client connections, sends questions, tracks scores, and handles game state.
- **Client**: Connects to the server, receives questions, answers them, and sends answers back to the server.

### Communication Protocol

The server and client communicate using both **TCP** and **UDP**:
- **TCP**: Used for reliable communication, such as sending messages about game start, score updates, and answers.
- **UDP**: Used for faster, less reliable communication, such as sending polling messages and broadcasting questions.

---

## Server Design

### Overview

The **Server** class is responsible for managing the entire game, including handling multiple client connections, sending questions, calculating scores, and managing the game flow.

### Components:
1. **Server.java**:
   - The main class that runs the server.
   - Listens for incoming client connections via TCP and assigns each client a `ClientThread`.
   - Coordinates game flow by sending questions and receiving answers.
   - Keeps track of the game state and scores for each connected player.

2. **ClientHandler.java**:
   - Manages communication with individual clients.
   - Each client has a `ClientThread` running on the server, handling their messages and interactions.
   - Handles receiving answers, updating scores, and sending acknowledgments to the client.

3. **ServerWindow.java**:
   - Provides a **GUI** for the server, allowing the server operator to see connected players, manage the game state, and view player scores.
   - Displays the current question and answers, as well as logs of server activity.

4. **Networking**:
   - The server listens on both **TCP** and **UDP** ports:
     - **TCP**: Manages reliable, stateful communication with the clients.
     - **UDP**: Used for sending polling requests and broadcasting messages to all clients.

### Workflow:
1. The server listens for incoming connections on a specified TCP port.
2. When a client connects, the server spawns a new `ClientThread` to handle that client's requests.
3. The server sends a **game start** message to all clients, followed by the questions.
4. Clients submit answers, and the server processes them to update scores.
5. At the end of the game, the server sends the **game over** message with the final scores.

---

## Client Design

### Overview

The **Client** class is responsible for interacting with the user, displaying questions, receiving answers, and communicating with the server.

### Components:
1. **TriviaClient.java**:
   - The main class that connects to the server via TCP and UDP.
   - Sends a join message to the server when the client starts.
   - Listens for incoming messages (e.g., questions, game status updates) from the server.
   - Handles user input and submits answers back to the server.
   - Displays the game’s UI for interacting with the player.

2. **ClientWindow.java**:
   - The GUI for the client, displaying questions, options, and buttons for the user to interact with.
   - Displays the score, timer, and poll/submit buttons.
   - Handles the enabling/disabling of buttons based on game state (polling, answering).
   
3. **NetworkHandler.java**:
   - Manages both the **TCP** and **UDP** connections between the client and server.
   - Handles sending and receiving messages to/from the server.
   - Listens for incoming messages from the server and processes them accordingly (e.g., question, score update, etc.).

4. **UDPThread.java**:
   - Handles UDP communication for polling and broadcasting messages.
   - Receives and processes server messages related to polling and game actions.

### Workflow:
1. The client connects to the server via **TCP** to join the game and receives game instructions.
2. It listens for **question** messages from the server and displays the question to the player.
3. The client sends a **polling message** when the player is ready to answer and waits for the server’s approval to proceed with the answer.
4. When the player answers, the client sends the selected option to the server and updates the score based on the server’s feedback.
5. Once the game ends, the client displays the final score and game results.

---

## Interaction Between Server and Client

### Game Flow:

1. **Server Setup**:
   - The server starts and waits for clients to join.
   - When a client joins, the server assigns them a unique ID and sends a "game start" message.

2. **Questioning Phase**:
   - The server sends the current question to all connected clients via TCP.
   - Each client receives the question and polls the server to indicate their readiness to answer.

3. **Answering Phase**:
   - After polling, the server grants permission for the client to submit an answer.
   - The client sends the answer via TCP, and the server evaluates it.
   - The server then updates the client with their score, and the next round begins.

4. **Game Over**:
   - When all questions have been answered, the server sends a game-over message to all clients, along with their final scores.

---

## Additional Notes

- **TCP vs UDP**: 
  - **TCP** ensures reliable message delivery, which is why it’s used for game-critical actions like submitting answers and scores.
  - **UDP** is used for less-critical actions like polling and broadcasting questions to all clients.
  
- **Scalability**: 
  - The server is capable of handling multiple clients simultaneously by spawning a `ClientThread` for each connection.
  
- **Error Handling**: 
  - Both the server and client include basic error handling for network failures, such as disconnections or timeouts.

---

## Conclusion

This design separates concerns between the server and client and uses both TCP and UDP protocols to efficiently manage game state and client interactions. The server manages the flow of the game, while the client handles user input and displays the game state.
