package client;

import java.util.Arrays;
import java.util.Scanner;

public class Repl {

    private final ServerFacade server;

    public Repl(int port) {
        server = new ServerFacade(port);
    }


    public void run() {
        System.out.println("Welcome to Chess. Type 'help' to get started");

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
                    System.out.println("Goodbye");
                    return;
                }

                String result = prelogin.eval(tokens);
                System.out.println(result);
            }
        }
    }
    private void printPrompt() {
        System.out.print("[LOGGED_OUT] >>> ");
    }
}
