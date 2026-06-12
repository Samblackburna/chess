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


import chess.ChessMove;
import chess.ChessGame;
import chess.InvalidMoveException;

import java.net.URI;

public class WebSocketFrontend extends Endpoint {

    private final Session session;
    private final WebSocketContainer container;
    private final ServerMessageHandler messageHandler;
    private static final Gson GSON = new Gson();

    public WebSocketFrontend(String serverUrl, ServerMessageHandler messageHandler) throws Exception {
        this.messageHandler = messageHandler;
        URI uri = new
                URI(serverUrl.replace("http", "ws") + "/ws");
        this.container = ContainerProvider.getWebSocketContainer();
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

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    public void sendConnect(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void sendMove(String authToken, int gameID, ChessMove move) throws Exception {
        send(new MakeMoveCommand(authToken, gameID, move));
    }

    public void sendLeave(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }

    public void sendResign(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    private void send(Object command) throws Exception {
        this.session.getBasicRemote().sendText(GSON.toJson(command));
    }
}
