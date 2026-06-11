package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class PostloginClient {

    private final ServerFacade server;
    private final AuthData auth;
    private final String serverUrl;
    private final Scanner scanner;
    private GameplayClient pendingGame;
    private List<GameData> lastGameList = new ArrayList<>();

    public PostloginClient(ServerFacade server, AuthData auth, String serverUrl, Scanner scanner) {
        this.server = server;
        this.auth = auth;
        this.serverUrl = serverUrl;
        this.scanner = scanner;
    }

    public GameplayClient getPendingGame() {
        GameplayClient game = pendingGame;
        pendingGame = null;
        return game;
    }

    // Returns true if the user logged out
    public boolean eval(String[] tokens) {
        String cmd = tokens.length > 0 ? tokens[0] : "help";
        String[] params = java.util.Arrays.copyOfRange(tokens, 1, tokens.length);
        switch (cmd) {
            case "logout"  -> { return logout(); }
            case "create"  -> createGame(params);
            case "list"    -> listGames();
            case "join"    -> joinGame(params);
            case "observe" -> observeGame(params);
            case "help"    -> System.out.println(help());
            default        -> System.out.println("Unknown command. Type 'help' for options.");
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

    private void createGame(String[] params) {
        if (params.length < 1) {
            System.out.println("Expected: create <game name>");
            return;
        }
        try {
            String gameName = String.join(" ", params);
            server.createGame(auth.authToken(), gameName);
            lastGameList = new ArrayList<>(server.listGames(auth.authToken()));
            System.out.println("Game '" + gameName + "' created");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            Collection<GameData> games = server.listGames(auth.authToken());
            lastGameList = new ArrayList<>(games);
            if (lastGameList.isEmpty()) {
                System.out.println("No games available");
                return;
            }
            for (int i = 0; i < lastGameList.size(); i++) {
                GameData game = lastGameList.get(i);
                String white = game.whiteUsername() != null ? game.whiteUsername() : "open";
                String black = game.blackUsername() != null ? game.blackUsername() : "open";
                System.out.printf("%d. %s  [white: %s | black: %s]%n", i + 1, game.gameName(), white, black);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void joinGame(String[] params) {
        if (params.length < 2) {
            System.out.println("Expected: join <game number> <WHITE|BLACK>");
            return;
        }
        try {
            if (lastGameList.isEmpty()) {
                lastGameList = new ArrayList<>(server.listGames(auth.authToken()));
            }
            if (params[0].equalsIgnoreCase("WHITE") || params[0].equalsIgnoreCase("BLACK")) {
                System.out.println("Wrong order: expected game number before color");
                return;
            }
            int ind = Integer.parseInt(params[0]) - 1;
            if (ind < 0 || ind >= lastGameList.size()) {
                System.out.println("Invalid game number. Use list to see available games.");
                return;
            }



            String color = params[1].toUpperCase();
            int gameID = lastGameList.get(ind).gameID();
            server.joinGame(auth.authToken(), gameID, color);
            ChessGame.TeamColor perspective = color.equals("BLACK")
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            pendingGame = new GameplayClient(serverUrl, auth.authToken(), gameID, perspective, scanner);
            pendingGame.sendConnect();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void observeGame(String[] params) {
        if (params.length < 1) {
            System.out.println("Expected: observe <game number>");
            return;
        }
        try {
            if (lastGameList.isEmpty()) {
                lastGameList = new ArrayList<>(server.listGames(auth.authToken()));
            }
            int ind = Integer.parseInt(params[0]) - 1;
            if (ind < 0 || ind >= lastGameList.size()) {
                System.out.println("Invalid game number. Use 'list' to see available games.");
                return;
            }
            int gameID = lastGameList.get(ind).gameID();
            pendingGame = new GameplayClient(serverUrl, auth.authToken(), gameID, ChessGame.TeamColor.WHITE, scanner);
            pendingGame.sendConnect();
        } catch (NumberFormatException e) {
            System.out.println("Game number must be a number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public String help() {
        return """
                  create <name>                    - create a new game
                  list                             - list all games
                  join <game number> <WHITE|BLACK>  - join a game
                  observe <game number>             - observe a game
                  logout                           - log out of your account
                  help                             - list available commands
                """;
    }
}
