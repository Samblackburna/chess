package dataaccess;

import model.*;
import java.util.*;
import java.sql.*;

public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
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
        throw new DataAccessException("not implemented");
    }

    public void createUser(UserData user) throws DataAccessException {
        throw new DataAccessException("not implemented");
    }

    public UserData getUser(String username) throws DataAccessException {
        throw new DataAccessException("not implemented");
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
        throw new DataAccessException("not implemented");
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        throw new DataAccessException("not implemented");
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        throw new DataAccessException("not implemented");
    }
}
