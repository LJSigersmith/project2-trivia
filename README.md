
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

## Game Flow

1. **Server**: Starts first and waits for clients to connect.
2. **Client**: Connects to the server and waits for game instructions.
3. **Poll Phase**: Clients can "poll" to answer questions. The server sends a question to all clients.
4. **Answer Phase**: Clients can submit their answers.
5. **Game Over**: The server calculates and broadcasts scores.
