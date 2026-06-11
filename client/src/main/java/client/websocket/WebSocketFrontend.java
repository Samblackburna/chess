package client.websocket;

import com.google.gson.Gson;
import jakarta.websocket.*;

import model.AuthData;
import model.GameData;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.commands.MakeMoveCommand;
import websocket.commands.*;
import websocket.messages.*;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;

import chess.ChessMove;
import chess.ChessGame;
import chess.InvalidMoveException;

import java.net.URI;

public class WebSocketFrontend extends Endpoint {

    private final Session session;
    private final ServerMessageHandler messageHandler;
    private static final Gson GSON = new Gson();

    public WebSocketFrontend(String serverUrl, ServerMessageHandler messageHandler) throws Exception {
        this.messageHandler = messageHandler;
        URI uri = new URI(serverUrl.replace("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String json) {
                ServerMessage base = GSON.fromJson(json, ServerMessage.class);
                ServerMessage message = switch (base.getServerMessageType()) {
                    case LOAD_GAME    -> GSON.fromJson(json, LoadGameMessage.class);
                    case ERROR        -> GSON.fromJson(json, ErrorMessage.class);
                    case NOTIFICATION -> GSON.fromJson(json, NotificationMessage.class);
                };
                messageHandler.handleServerMessage(message);
            }
        });
    }

    public void Move(string authToken, int gameID) throws exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());

        if (game.isGameOver()) {
            return;
        }

        boolean isWhiteTurn = game.getTeamTurn() == ChessGame.TeamColor.WHITE;
        boolean isCorrectPlayer = (isWhiteTurn && auth.username().equals(gameData.whiteUsername()))
                || (!isWhiteTurn && auth.username().equals(gameData.blackUsername()));

        if (!isCorrectPlayer) {
            return;
        }

        MakeMoveCommand moveCommand = GSON.fromJson(ctx.message(), MakeMoveCommand.class);
        try {
            game.makeMove(moveCommand.getMove());
        } catch (InvalidMoveException e) {
            return;
        }
    }

}
