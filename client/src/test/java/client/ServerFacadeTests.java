package client;

import org.junit.jupiter.api.*;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        // specific to extra facade package we added
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clearDatabase();
    }

    // register
    @Test
    void registerSuccess() throws Exception {
        var auth = facade.register("sam", "password", "sam@email.com");
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("sam", auth.username());
    }

    @Test
    void registerDuplicateUsername() throws Exception {
        facade.register("sam", "password", "sam@email.com");
        Assertions.assertThrows(Exception.class, () ->
                facade.register("sam", "other", "other@email.com"));
    }

    // login
    @Test
    void loginSuccess() throws Exception {
        facade.register("sam", "password", "sam@email.com");
        var auth = facade.login("sam", "password");
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("sam", auth.username());
    }
    
    @Test
    void loginWrongPassword() {
        Assertions.assertThrows(Exception.class, () ->
                facade.login("sam", "wrongpassword"));
    }

    // logout
    @Test
    void logoutSuccess() throws Exception {
        var auth = facade.register("sam", "password", "sam@email.com");
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutInvalidToken() {
        Assertions.assertThrows(Exception.class, () ->
                facade.logout("not-a-real-token"));
    }

    // createGame
    @Test
    void createGameSuccess() throws Exception {
        var auth = facade.register("sam", "password", "sam@email.com");
        int gameID = facade.createGame(auth.authToken(), "My Game");
        Assertions.assertTrue(gameID > 0);
    }

    @Test
    void createGameUnauthorized() {
        Assertions.assertThrows(Exception.class, () ->
                facade.createGame("bad-token", "My Game"));
    }



    // joinGame
    @Test
    void joinGameSuccess() throws Exception {
        var auth = facade.register("sam", "password", "sam@email.com");
        int gameID = facade.createGame(auth.authToken(), "My game");

        Assertions.assertDoesNotThrow(() ->
                facade.joinGame(auth.authToken(), gameID, "WHITE"));
    }


    @Test
    void joinGameColorTaken() throws Exception {
        var auth1 = facade.register("sam", "password", "sam@email.com");
        var auth2 = facade.register("bob", "password", "bob@email.com");
        int gameID = facade.createGame(auth1.authToken(), "My Game");
        facade.joinGame(auth1.authToken(), gameID, "WHITE");
        Assertions.assertThrows(Exception.class, () ->
                facade.joinGame(auth2.authToken(), gameID, "WHITE"));
    }




    // listGames
    @Test
    void listGamesSuccess() throws Exception {
        var auth = facade.register("sam", "password", "sam@email.com");
        facade.createGame(auth.authToken(), "game one");
        facade.createGame(auth.authToken(), "game two");
        var games = facade.listGames(auth.authToken());
        Assertions.assertEquals(2, games.size());
    }

    @Test
    void listGamesUnauthorized() {
        Assertions.assertThrows(Exception.class, () ->
                facade.listGames("bad token"));
    }


}
