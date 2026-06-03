package client;

import model.AuthData;


public class PostloginClient {

    private final ServerFacade server;
    private final AuthData auth;

    public PostloginClient(ServerFacade server, AuthData auth) {
        this.server = server;
        this.auth = auth;
    }

    // Returns true if the user logged out
    public boolean eval(String[] tokens) {
        String cmd = tokens.length > 0 ? tokens[0] : "help";
        switch (cmd) {
            case "logout" -> { return logout(); }
            case "help"   -> System.out.println(help());
            default       -> System.out.println("Unknown command. Type 'help' for options.");
        }
        return false;
    }


    private boolean logout() {
        try {
            server.logout(auth.authToken());
            System.out.println("Logged out successfully.");
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public String help() {
        return """
                  logout       - log out of your account
                  help         - list available commands
                """;
    }
}
