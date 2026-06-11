package client;

import chess.*;
import client.websocket.ServerMessageHandler;
import client.websocket.WebSocketFrontend;
import ui.CreateBoard;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class GameplayClient implements ServerMessageHandler {

    private final WebSocketFrontend ws;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor perspective;
    private final Scanner scanner;
    private volatile ChessGame currentGame;

    public GameplayClient(String serverUrl, String authToken, int gameID,
                          ChessGame.TeamColor perspective, Scanner scanner) throws Exception {
        this.ws = new WebSocketFrontend(serverUrl, this);
        this.authToken = authToken;
        this.gameID = gameID;
        this.perspective = perspective;
        this.scanner = scanner;
    }

    public void sendConnect() throws Exception {
        ws.sendConnect(authToken, gameID);
    }



    // Returns true when the user leaves, signaling Repl to return to post-login
    public boolean eval(String[] tokens) {
        String cmd = tokens.length > 0 ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        try {
            switch (cmd) {
                case "help" -> System.out.println(help());
                case "redraw" -> redraw();
                // case "leave"     -> { leave(); return true; }
                // case "move"      -> move(params);
                // case "resign"    -> resign();
                // case "highlight" -> highlight(params);
                default -> System.out.println("Unknown command. Type 'help' for options.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }


    @Override
    public void handleServerMessage(ServerMessage message) {
        System.out.println();
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                currentGame = ((LoadGameMessage) message).getGame();
                redraw();
            }
            case NOTIFICATION -> System.out.println(((NotificationMessage) message).getMessage());
            case ERROR -> System.out.println(((ErrorMessage) message).getErrorMessage());
        }
        printPrompt();
    }

    private void redraw() {
        if (currentGame != null) {
            new CreateBoard().draw(currentGame.getBoard(), perspective);
        }
    }

    public void printPrompt() {
        System.out.print("[IN_GAME] >>> ");
    }

    private String help() {
        return """
                  help                           - show available commands
                  redraw                         - redraw the chess board
                  move <from> <to> [promotion]   - make a move (e.g. move e2 e4)
                  highlight <position>           - show legal moves for a piece (e.g. highlight e2)
                  resign                         - forfeit the game
                  leave                          - leave the game
                """;
    }
}