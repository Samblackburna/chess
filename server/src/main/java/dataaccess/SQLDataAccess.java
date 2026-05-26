package dataaccess;

import model.*;
import java.util.*;
import java.sql.*;

public class SQLDataAccess implements DataAccess {

    public SQLDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    public void clear() throws DataAccessException {
        throw new DataAccessException("not implemented");
    }

    private void configureDatabase() throws DataAccessException {

    }

    public void createUser(UserData user) throws DataAccessException {

    }

    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    public int createGame(GameData game) throws DataAccessException {
        return 0;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return null;
    }

    public void updateGame(GameData game) throws DataAccessException {
    }

    public void createAuth(AuthData auth) throws DataAccessException {

    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {

    }
}
