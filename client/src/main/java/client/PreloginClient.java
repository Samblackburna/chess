/*
According to Single Responsibility Principle: we're breaking out
 */

package client;

import java.util.Arrays;
import java.util.Scanner;

public class PreloginClient {

    private final ServerFacade server;

    public PreloginClient(ServerFacade server) {
        this.server = server;
    }

    public String eval(String[] tokens) {
        String cmd = tokens.length > 0 ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "help" -> help();
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String register(String[] params) {
        if (params.length < 3) {
            return "Expected: register <username> <password> <email>";
        }
        try {
            var auth = server.register(params[0], params[1], params[2]);
            return String.format("Registered and logged in as %s.", auth.username());
        } catch (Exception e) {
            // return null;
            return "Error: " + e.getMessage();

        }
    }



    private String login(String[] params) {
        return "not implemented.";
    }

    public String help() {
        return """
                  register <username> <password> <email> - create an account
                  login <username> <password>            - log in to an existing account
                  quit                                   - exit the program
                  help                                   - list available commands
                """;
    }
}
