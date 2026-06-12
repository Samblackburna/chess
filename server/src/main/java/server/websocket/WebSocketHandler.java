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
import chess.ChessGame;
import chess.InvalidMoveException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
// for new methods
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;
import org.eclipse.jetty.websocket.api.Session;

import java.time.Duration;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final DataAccess dataAccess;
    private final ConnectionManager connections = new ConnectionManager();
    private static final Gson GSON = new Gson();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // I'm timing out, so we're trying this
    @Override
    public void handleConnect(WsConnectContext ctx) {
        ctx.session.setIdleTimeout(Duration.ofHours(1));
    }

    @Override
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

    @Override
    public void handleClose(WsCloseContext ctx) {
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx.session, "Error: invalid auth token");
            return;
        }
        GameData game = dataAccess.getGame(command.getGameID());
        if (game == null) {
            sendError(ctx.session, "Error: game not found");
            return;
        }

        connections.add(command.getGameID(), auth.username(), ctx.session);

        String loadGameJson = GSON.toJson(new LoadGameMessage(game.game()));
        connections.sendToUser(auth.username(), loadGameJson);

        String notificationText;
        if (auth.username().equals(game.whiteUsername())) {
            notificationText = auth.username() + " added to WHITE";
        } else if (auth.username().equals(game.blackUsername())) {
            notificationText = auth.username() + " added to BLACK";
        } else {
            notificationText = auth.username() + " is an observer";
        }
        String notificationJson = GSON.toJson(new NotificationMessage(notificationText));
        connections.broadcastExcluding(command.getGameID(), auth.username(), notificationJson);
    }

    private void sendError(Session session, String message) throws Exception {
        String json = GSON.toJson(new ErrorMessage(message));
        session.getRemote().sendString(json);
    }


    private void makeMove(WsMessageContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx.session, "Error: invalid auth token");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(ctx.session, "Error: game not found");
            return;
        }

        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            sendError(ctx.session, "Error: game already over");
            return;
        }

        boolean isWhiteTurn = game.getTeamTurn() == ChessGame.TeamColor.WHITE;
        boolean isCorrectPlayer = (isWhiteTurn && auth.username().equals(gameData.whiteUsername()))
                || (!isWhiteTurn && auth.username().equals(gameData.blackUsername()));

        if (!isCorrectPlayer) {
            sendError(ctx.session, "Error: it is not your turn");
            return;
        }

        MakeMoveCommand moveCommand = GSON.fromJson(ctx.message(), MakeMoveCommand.class);
        try {
            game.makeMove(moveCommand.getMove());
        } catch (InvalidMoveException e) {
            // needed to pass Make Invalid Move Test
            sendError(ctx.session, "Error: " + e.getMessage());
            return;
        }
        dataAccess.updateGame(gameData);

        String loadGameJson = GSON.toJson(new LoadGameMessage(game));
        connections.broadcast(command.getGameID(), loadGameJson);

        String moveNotification = auth.username() + " moved " + moveCommand.getMove();
        connections.broadcastExcluding(command.getGameID(), auth.username(),
                GSON.toJson(new NotificationMessage(moveNotification)));
        //


        ChessGame.TeamColor nextTeam = game.getTeamTurn();
        if (game.isInCheckmate(nextTeam)) {
            game.setGameOver(true);
            dataAccess.updateGame(gameData);
            connections.broadcast(command.getGameID(),
                    GSON.toJson(new NotificationMessage(nextTeam + " is in checkmate. Game over!")));
        } else if (game.isInStalemate(nextTeam)) {
            game.setGameOver(true);
            dataAccess.updateGame(gameData);
            connections.broadcast(command.getGameID(),
                    GSON.toJson(new NotificationMessage("Stalemate. Game over!")));
        } else if (game.isInCheck(nextTeam)) {
            connections.broadcast(command.getGameID(),
                    GSON.toJson(new NotificationMessage(nextTeam + " is in check!")));
        }
    }

    private void leave(WsMessageContext ctx, UserGameCommand command) throws Exception {
        // see connections above for reference
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx.session, "Error: invalid auth token");
            return;
        }
        GameData game = dataAccess.getGame(command.getGameID());
        if (game == null) {
            sendError(ctx.session, "Error: game not found");
            return;
        }


        if (auth.username().equals(game.whiteUsername())) {
            GameData updated = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
            dataAccess.updateGame(updated);

        } else if (auth.username().equals(game.blackUsername())) {
            GameData updated = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
            dataAccess.updateGame(updated);
        }

        connections.remove(auth.username());

        String notificationJson = GSON.toJson(new NotificationMessage(auth.username() + " left game"));
        connections.broadcast(command.getGameID(), notificationJson);
    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx.session, "Error: invalid auth token");
            return;
        }
        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(ctx.session, "Error: game not found");
            return;
        }

        boolean isPlayer = auth.username().equals(gameData.whiteUsername())
                || auth.username().equals(gameData.blackUsername());
        if (!isPlayer) {
            sendError(ctx.session, "Error: observers cannot resign");
            return;
        }

        if (gameData.game().isGameOver()) {
            sendError(ctx.session, "Error: game already over");
            return;
        }

        gameData.game().setGameOver(true);
        dataAccess.updateGame(gameData);

        String notificationJson = GSON.toJson(new NotificationMessage(auth.username() + " resigned. Game over"));
        connections.broadcast(command.getGameID(), notificationJson);

    }
}

