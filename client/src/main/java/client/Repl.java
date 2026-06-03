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
                        var postlogin = new PostloginClient(server, auth);
                        while (state == State.SIGNEDIN) {
                            printPrompt();
                            String postLine = scanner.nextLine().trim();
                            if (postLine.isBlank()) { continue; }
                            String[] postTokens = postLine.toLowerCase().split("\\s+");

                            if (postTokens[0].equals("quit")) {
                                System.out.println("Goodbye!");
                                return;
                            }
                            if (postlogin.eval(postTokens)) {
                                state = State.SIGNEDOUT;
                            }
                        }
                    }
                }

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
