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
                case "help"      -> System.out.println(help());
                case "redraw"    -> redraw();
                case "leave"     -> { leave(); return true; }
                case "move"      -> move(params);
                case "resign"    -> resign();
                case "highlight" -> highlight(params);
                default -> System.out.println("Unknown command. Type 'help' for options.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    private void leave() throws Exception {
        ws.sendLeave(authToken, gameID);
    }

    private void move(String[] params) throws Exception {
        if (params.length < 2) {
            System.out.println("Expected: move <from> <to> [promotion]  e.g. move e2 e4");
            return;
        }
        ChessPosition from = parsePosition(params[0]);
        ChessPosition to = parsePosition(params[1]);
        ChessPiece.PieceType promotion = null;
        if (params.length >= 3) {
            promotion = parsePromotion(params[2]);
        }
        ws.sendMove(authToken, gameID, new ChessMove(from, to, promotion));
    }

    private void resign() throws Exception {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.equals("yes")) {
            ws.sendResign(authToken, gameID);
        } else if (answer.equals("no")) {
            System.out.println("Resign cancelled.");
        } else {
            System.out.println("Resign cancelled.");
        }
    }

    private void highlight(String[] params) {
        if (params.length < 1) {
            System.out.println("Expected: highlight <position>  e.g. highlight e2");
            return;
        }
        if (currentGame == null) {
            System.out.println("No game loaded yet.");
            return;
        }
        ChessPosition pos = parsePosition(params[0]);
        Collection<ChessMove> moves = currentGame.validMoves(pos);
        Set<ChessPosition> targets = new HashSet<>();
        if (moves != null) {
            for (ChessMove m : moves) {
                targets.add(m.getEndPosition());
            }
        }
        new CreateBoard().drawWithHighlights(currentGame.getBoard(), perspective, pos, targets);
    }

    private ChessPosition parsePosition(String s) {
        if (s.length() != 2) {
            throw new IllegalArgumentException("Invalid position '" + s + "'. Use format like e2.");
        }
        int col = s.charAt(0) - 'a' + 1;
        int row = s.charAt(1) - '0';
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Position out of bounds: " + s);
        }
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String s) {
        return switch (s.toLowerCase()) {
            case "queen",  "q" -> ChessPiece.PieceType.QUEEN;
            case "rook",   "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException(
                    "Invalid promotion '" + s + "'. Use queen, rook, bishop, or knight.");
        };
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