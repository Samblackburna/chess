package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(int gameID) {}
    public record JoinGameRequest(String playerColor, Integer gameID) {}

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return dataAccess.listGames();
    }

    public CreateGameResult createGame(CreateGameRequest req, String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (req.gameName() == null) {
            throw new BadRequestException("Error: bad request");
        }

        int gameID = dataAccess.createGame(new GameData(0, null, null, req.gameName(), new ChessGame()));
        return new CreateGameResult(gameID);
    }

    public void joinGame(JoinGameRequest req, String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if (req.gameID() == null) {
            throw new BadRequestException("Error: bad request");
        }
        GameData game = dataAccess.getGame(req.gameID());
        if (game == null) {
            throw new BadRequestException("Error: bad request");
        }

        ChessGame.TeamColor color;
        try {
            if (req.playerColor() == null || req.playerColor().isEmpty()) {
                throw new BadRequestException("Error: bad request");
            }

            color = ChessGame.TeamColor.valueOf(req.playerColor());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Error: bad request");
        }

        GameData updatedGame;
        if (color == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
        } else {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
        }
        dataAccess.updateGame(updatedGame);
    }
}
