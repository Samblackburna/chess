/*
According to Single Responsibility Principle: we're breaking out Petshop's PetClient code into a bunch of different classes
 */

package client;

import model.AuthData;

import java.util.Arrays;

public class PreloginClient {

    private final ServerFacade server;

    public PreloginClient(ServerFacade server) {
        this.server = server;
    }

    // Returns AuthData on successful login/register, null otherwise.
    // Repl uses the return value to detect when to transition to postlogin.
    public AuthData eval(String[] tokens) {
        String cmd = tokens.length > 0 ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        switch (cmd) {
            case "register" -> { return register(params); }
            case "login"    -> { return login(params); }
            case "help"     -> System.out.println(help());
            default         -> System.out.println("Unknown command. Type 'help' for options.");
        }
        return null;
    }

    private AuthData register(String[] params) {
        if (params.length < 3) {
            System.out.println("Expected: register <username> <password> <email>");
            return null;
        }
        try {
            var auth = server.register(params[0], params[1], params[2]);
            System.out.println("Registered and logged in as " + auth.username() + ".");
            return auth;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    private AuthData login(String[] params) {
        if (params.length < 2) {
            System.out.println("Expected: login <username> <password>");
            return null;
        }
        try {
            var auth = server.login(params[0], params[1]);
            System.out.println("Logged in as " + auth.username() + ".");
            return auth;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
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
