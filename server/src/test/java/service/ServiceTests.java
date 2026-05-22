package service;

import dataaccess.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private DataAccess dataAccess;
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        clearService = new ClearService(dataAccess);
    }

    @Test
    void registerSuccess() throws DataAccessException {
        var result = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        assertEquals("sam", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void registerAlreadyTaken() throws DataAccessException {
        userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        assertThrows(AlreadyTakenException.class, () ->
                userService.register(new UserService.RegisterRequest("sam", "pass2", "other@mail.com")));
    }

    @Test
    void loginSuccess() throws DataAccessException {
        userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        var result = userService.login(new UserService.LoginRequest("sam", "pass"));
        assertEquals("sam", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void loginWrongPassword() throws DataAccessException {
        userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        assertThrows(UnauthorizedException.class, () ->
                userService.login(new UserService.LoginRequest("sam", "wrongpassword")));
    }

    @Test
    void logoutSuccess() throws DataAccessException {
        var reg = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        userService.logout(reg.authToken());
        assertNull(dataAccess.getAuth(reg.authToken()));
    }

    @Test
    void logoutInvalidToken() {
        assertThrows(UnauthorizedException.class, () -> userService.logout("fake-token"));
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        var reg = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        gameService.createGame(new GameService.CreateGameRequest("Game 1"), reg.authToken());
        var games = gameService.listGames(reg.authToken());
        assertEquals(1, games.size());
    }

    @Test
    void listGamesUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("bad-token"));
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        var reg = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        var result = gameService.createGame(new GameService.CreateGameRequest("My Game"), reg.authToken());
        assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameBadRequest() throws DataAccessException {
        var reg = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        assertThrows(BadRequestException.class, () ->
                gameService.createGame(new GameService.CreateGameRequest(null), reg.authToken()));
    }

    @Test
    void joinGameSuccess() throws DataAccessException {
        var reg = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        var game = gameService.createGame(new GameService.CreateGameRequest("My Game"), reg.authToken());
        gameService.joinGame(new GameService.JoinGameRequest("WHITE", game.gameID()), reg.authToken());
        assertEquals("sam", dataAccess.getGame(game.gameID()).whiteUsername());
    }

    @Test
    void joinGameAlreadyTaken() throws DataAccessException {
        var reg1 = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        var reg2 = userService.register(new UserService.RegisterRequest("bob", "pass", "bob@mail.com"));
        var game = gameService.createGame(new GameService.CreateGameRequest("My Game"), reg1.authToken());
        gameService.joinGame(new GameService.JoinGameRequest("WHITE", game.gameID()), reg1.authToken());
        assertThrows(AlreadyTakenException.class, () ->
                gameService.joinGame(new GameService.JoinGameRequest("WHITE", game.gameID()), reg2.authToken()));
    }

    @Test
    void clearSuccess() throws DataAccessException {
        userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        clearService.clear();
        assertNull(dataAccess.getUser("sam"));
    }
}