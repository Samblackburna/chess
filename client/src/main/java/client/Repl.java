package client;

import model.AuthData;

import java.util.Scanner;

public class Repl {

    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public Repl(int port) {
        server = new ServerFacade(port);
    }

    public void run() {
        System.out.println("Welcome to Chess. Type 'help' to get started.");

        var prelogin = new PreloginClient(server);

        try (var scanner = new Scanner(System.in)) {
            while (true) {
                printPrompt();
                String line = scanner.nextLine().trim();
                if (line.isBlank()) {
                    continue;
                }

                String[] tokens = line.toLowerCase().split("\\s+");

                if (tokens[0].equals("quit")) {
                    System.out.println("Goodbye!");
                    return;
                }

                if (state == State.SIGNEDOUT) {
                    AuthData auth = prelogin.eval(tokens);
                    if (auth != null) {
                        state = State.SIGNEDIN;
                        runPostlogin(scanner, auth);
                    }
                }

            }
        }
    }

    private void runPostlogin(Scanner scanner, AuthData auth) {
        var postlogin = new PostloginClient(server, auth, server.getServerUrl(), scanner);
        while (state == State.SIGNEDIN) {
            printPrompt();
            String line = scanner.nextLine().trim();
            if (line.isBlank()) {
                continue;
            }
            String[] tokens = line.toLowerCase().split("\\s+");
            if (tokens[0].equals("quit")) {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            if (postlogin.eval(tokens)) {
                state = State.SIGNEDOUT;
            } else {
                GameplayClient game = postlogin.getPendingGame();
                if (game != null) {
                    runGameplay(scanner, game);
                }
            }
        }
    }

    private void runGameplay(Scanner scanner, GameplayClient game) {
        state = State.INGAME;
        while (state == State.INGAME) {
            game.printPrompt();
            String line = scanner.nextLine().trim();
            if (line.isBlank()) {
                continue;
            }
            String[] tokens = line.toLowerCase().split("\\s+");
            if (tokens[0].equals("quit")) {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            if (game.eval(tokens)) {
                state = State.SIGNEDIN;
            }
        }
    }

    private void printPrompt() {
        if (state == State.SIGNEDOUT) {
            System.out.print("[LOGGED_OUT] >>> ");
        } else {
            System.out.print("[LOGGED_IN] >>> ");
        }
    }
}
