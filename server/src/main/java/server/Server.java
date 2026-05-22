package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final DataAccess dataAccess;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;
    private static final Gson GSON = new Gson(); // this will eventually connect to JSON library

    public Server() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        clearService = new ClearService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        /* javalin exception handling. 
        app.exception(BadRequestResponse.class, (e, ctx) -> {
        ctx.json("Bad request: ${e.message}.").status(400)
        });
        (see SO)
        */

        javalin.exception(BadRequestException.class, (e, ctx) -> error(ctx, 400, e.getMessage()));
        javalin.exception(UnauthorizedException.class, (e, ctx) -> error(ctx, 401, e.getMessage()));
        javalin.exception(AlreadyTakenException.class, (e, ctx) -> error(ctx, 403, e.getMessage()));

        javalin.exception(Exception.class, (e, ctx) -> error(ctx, 500, "Error: " + e.getMessage()));

        javalin.delete("/db",      this::clear);
        javalin.post("/user",      this::register);
        javalin.post("/session",   this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game",       this::listGames);
        javalin.post("/game",      this::createGame);
        javalin.put("/game",       this::joinGame);
    }

    // in starter code. DO NOT ALTER
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
    // end starrter code block

    private void clear(Context ctx) throws DataAccessException {
        clearService.clear();
        ctx.status(200).result("{}").contentType("application/json");
    }

    private void register(Context ctx) throws DataAccessException {
        var req = GSON.fromJson(ctx.body(), UserService.RegisterRequest.class);
        var result = userService.register(req);
        ctx.status(200).result(GSON.toJson(result)).contentType("application/json");
    }

    private void login(Context ctx) throws DataAccessException {
        var req = GSON.fromJson(ctx.body(), UserService.LoginRequest.class);
        var result = userService.login(req);
        ctx.status(200).result(GSON.toJson(result)).contentType("application/json");
    }

    private void logout(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        userService.logout(authToken);
        ctx.status(200).result("{}").contentType("application/json");
    }

    private void listGames(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        var games = gameService.listGames(authToken);
        ctx.status(200).result(GSON.toJson(Map.of("games", games))).contentType("application/json");
    }

    private void createGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        var req = GSON.fromJson(ctx.body(), GameService.CreateGameRequest.class);
        var result = gameService.createGame(req, authToken);
        ctx.status(200).result(GSON.toJson(result)).contentType("application/json");
    }

    private void joinGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        var req = GSON.fromJson(ctx.body(), GameService.JoinGameRequest.class);
        gameService.joinGame(req, authToken);
        ctx.status(200).result("{}").contentType("application/json");
    }

    private void error(Context ctx, int status, String message) {
        ctx.status(status).result(GSON.toJson(Map.of("message", message))).contentType("application/json");
    }
}
