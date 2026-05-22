package dataaccess;

import model.*;
import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();

    private int nextGameID = 1;

    public void clear() {
        users.clear();
        nextGameID = 1;
    }

    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists: " + user.username());
        }
        users.put(user.username(), user);
    }

    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }


}
