package dataaccess;

import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mindrot.jbcrypt.BCrypt;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.GameData;
import chess.InvalidMoveException;
import java.util.Collection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLDataAccessTest {
    private static MySQLDataAccess dataAccess;

    @BeforeAll
    static void setup() throws DataAccessException {
        dataAccess = new MySQLDataAccess();
    }

    @BeforeEach
    void reset() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
    @DisplayName("Clear: positive")
    void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("sam", "password", "sam@email.com"));
        dataAccess.clear();
        assertNull(dataAccess.getUser("sam"));
    }

    @Test
    @DisplayName("createUser: positive")
    void createUserSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("sam", "password", "sam@email.com"));
        var user = dataAccess.getUser("sam");
        assertNotNull(user);
        // assert equals code
        assertEquals("sam", user.username());
        assertEquals("sam@email.com", user.email());
    }

    @Test
    @DisplayName("createUser: negative - duplicate username")
    void createUserDuplicate() {
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createUser(new UserData("sam", "password", "sam@email.com"));
            dataAccess.createUser(new UserData("sam", "other", "other@email.com"));
        });
    }

    @Test
    @DisplayName("getUser: positive")
    void getUserSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("sam", "password", "sam@email.com"));
        var user = dataAccess.getUser("sam");
        assertEquals("sam", user.username());
        assertEquals("sam@email.com", user.email());
        assertTrue(BCrypt.checkpw("password", user.password()));
    }


    @Test
    @DisplayName("getUser: negative - nonexistent user")
    void getUserNotFound() throws DataAccessException {
        assertNull(dataAccess.getUser("nobody"));
    }

    @Test
    @DisplayName("createAuth: positive")
    void createAuthSuccess() throws DataAccessException {
        dataAccess.createAuth(new AuthData("token123", "sam"));
        assertNotNull(dataAccess.getAuth("token123"));
    }

    @Test
    @DisplayName("createAuth: negative - duplicate token")
    void createAuthDuplicate() {
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createAuth(new AuthData("token123", "sam"));
            dataAccess.createAuth(new AuthData("token123", "other"));
        });
    }

    @Test
    @DisplayName("getAuth: positive")
    void getAuthSuccess() throws DataAccessException {
        dataAccess.createAuth(new AuthData("token123", "sam"));
        var auth = dataAccess.getAuth("token123");
        assertEquals("token123", auth.authToken());
        assertEquals("sam", auth.username());
    }

    @Test
    @DisplayName("getAuth: negative - nonexistent token")
    void getAuthNotFound() throws DataAccessException {
        assertNull(dataAccess.getAuth("doesnotexist"));
    }


    @Test
    @DisplayName("deleteAuth: positive")
    void deleteAuthSuccess() throws DataAccessException {
        dataAccess.createAuth(new AuthData("token123", "sam"));
        dataAccess.deleteAuth("token123");
        assertNull(dataAccess.getAuth("token123"));
    }

    @Test
    @DisplayName("deleteAuth: negative - nonexistent token")
    void deleteAuthNotFound() {
        assertThrows(DataAccessException.class, () ->
                dataAccess.deleteAuth("doesnotexist"));
    }

    @Test
    @DisplayName("createGame: positive")
    void createGameSuccess() throws DataAccessException {
        int id = dataAccess.createGame(new GameData(0, null, null, "Test Game", new ChessGame()));
        assertTrue(id > 0);
    }

    @Test
    @DisplayName("createGame: negative - duplicate game name")
    void createGameDuplicate() throws DataAccessException {
        dataAccess.createGame(new GameData(0, null, null, "Test Game", new ChessGame()));
        dataAccess.createGame(new GameData(0, null, null, "Test Game", new ChessGame()));
        assertEquals(2, dataAccess.listGames().size());
    }

    @Test
    @DisplayName("getGame: positive")
    void getGameSuccess() throws DataAccessException {
        int id = dataAccess.createGame(new GameData(0, null, null, "Test Game", new ChessGame()));
        var game = dataAccess.getGame(id);
        assertNotNull(game);
        assertEquals("Test Game", game.gameName());
    }


    @Test
    @DisplayName("getGame: negative - nonexistent game")
    void getGameNotFound() throws DataAccessException {
        assertNull(dataAccess.getGame(99999));
    }

    @Test
    @DisplayName("listGames: positive")
    void listGamesSuccess() throws DataAccessException {
        dataAccess.createGame(new GameData(0, null, null, "Game 1", new ChessGame()));
        dataAccess.createGame(new GameData(0, null, null, "Game 2", new ChessGame()));
        // mirrored assert equals code
        assertEquals(2, dataAccess.listGames().size());
    }

    @Test
    @DisplayName("listGames: negative - empty list")
    void listGamesEmpty() throws DataAccessException {
        assertEquals(0, dataAccess.listGames().size());
    }

    @Test
    @DisplayName("updateGame: positive - board state persists")
    void updateGameSuccess() throws DataAccessException, InvalidMoveException {
        int id = dataAccess.createGame(new GameData(0, null, null, "Test Game", new ChessGame()));
        var gameData = dataAccess.getGame(id);

        gameData.game().makeMove(new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null));
        dataAccess.updateGame(gameData);
        var updated = dataAccess.getGame(id);
        assertEquals(gameData.game(), updated.game());
    }


    @Test
    @DisplayName("updateGame: negative - nonexistent game")
    void updateGameNotFound() {
        assertThrows(DataAccessException.class, () ->
                dataAccess.updateGame(new GameData(99999, null, null, "Ghost", new ChessGame())));

    }

}
