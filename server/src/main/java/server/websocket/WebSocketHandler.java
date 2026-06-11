package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
// for new methods
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;
import org.eclipse.jetty.websocket.api.Session;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final DataAccess dataAccess;
    private final ConnectionManager connections = new ConnectionManager();
    private static final Gson GSON = new Gson();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void handleConnect(WsConnectContext ctx) {
    }

    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = GSON.fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> makeMove(ctx, command);
                case LEAVE -> leave(ctx, command);
                case RESIGN -> resign(ctx, command);
            }
        } catch (Exception e) {}
    }

    public void handleClose(WsCloseContext ctx) {
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx.session, "invalid auth token");
        }
        GameData game = dataAccess.getGame(command.getGameID());
        if (game == null) {
            sendError(ctx.session, "game not found");
        }

        connections.add(command.getGameID(), auth.username(), ctx.session);

        String loadGameJson = GSON.toJson(new LoadGameMessage(game.game()));
        connections.sendToUser(auth.username(), loadGameJson);

        String notificationText;
        if (auth.username().equals(game.whiteUsername())) {
            notificationText = auth.username() + "WHITE";
        } else if (auth.username().equals(game.blackUsername())) {
            notificationText = auth.username() + "BLACK";
        } else {
            notificationText = auth.username() + "observer";
        }
        String notificationJson = GSON.toJson(new NotificationMessage(notificationText));
        connections.broadcastExcluding(command.getGameID(), auth.username(), notificationJson);
    }


    private void sendError(Session session, String message) throws Exception {
        String json = GSON.toJson(new ErrorMessage(message));
        session.getRemote().sendString(json);
    }

    private void makeMove(WsMessageContext ctx, UserGameCommand command) throws Exception {
    }

    private void leave(WsMessageContext ctx, UserGameCommand command) throws Exception {
    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws Exception {
    }
}

