package dataaccess;

import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mindrot.jbcrypt.BCrypt;

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
}
