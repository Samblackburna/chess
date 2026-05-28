package dataaccess;

import model.*;
import java.util.*;
import java.sql.*;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;
import org.mindrot.jbcrypt.BCrypt;

public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    // see MySqlDataAccess in Petshop
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    }
                    else if (param instanceof Integer p) {
                        ps.setInt(i + 1, p);
                    }
                    else if (param == null) {
                        ps.setNull(i + 1, NULL);
                    }
                }

                int rowsAffected = ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return rowsAffected;
            }
        }
        catch (SQLException e) {
            throw new DataAccessException("Database update failed: " + e.getMessage());
        }
    }

    private void configureDatabase() throws DataAccessException {
        String[] createStatements = {
                """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(256) PRIMARY KEY,
                    password VARCHAR(256) NOT NULL,
                    email VARCHAR(256) NOT NULL
                ) 
                """,
                """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    auth_token VARCHAR(256) PRIMARY KEY,
                    username VARCHAR(256) NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS games (
                    game_id        INT AUTO_INCREMENT PRIMARY KEY,
                    white_username VARCHAR(256),
                    black_username VARCHAR(256),
                    game_name      VARCHAR(256) NOT NULL,
                    game           TEXT         NOT NULL
                )
                """
        };

        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to Configure");
        }
    }

    public void clear() throws DataAccessException {
        executeUpdate("DELETE FROM auth_tokens");
        executeUpdate("DELETE FROM games");
        executeUpdate("DELETE FROM users");
    }

    public void createUser(UserData user) throws DataAccessException {
        var hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        executeUpdate("INSERT INTO users (username, password, email) VALUES (?, ?, ?)",
                user.username(), hashedPassword, user.email());
    }

    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user: " + e.getMessage());
        }
        return null;
    }

    public int createGame(GameData game) throws DataAccessException {
        throw new DataAccessException("not implemented");
    }

    public GameData getGame(int gameID) throws DataAccessException {
        throw new DataAccessException("not implemented");
    }

    public Collection<GameData> listGames() throws DataAccessException {
        throw new DataAccessException("not implemented");
    }

    public void updateGame(GameData game) throws DataAccessException {
        throw new DataAccessException("not implemented");
    }

    public void createAuth(AuthData auth) throws DataAccessException {
        executeUpdate("INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)",
                auth.authToken(), auth.username());
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT auth_token, username FROM auth_tokens WHERE auth_token=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(
                                rs.getString("auth_token"),
                                rs.getString("username")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get auth: " + e.getMessage());
        }
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        var rowsAffected = executeUpdate("DELETE FROM auth_tokens WHERE auth_token=?", authToken);


        // error response
        if (rowsAffected == 0) {
            throw new DataAccessException("Auth token not found");
        }
    }
}
