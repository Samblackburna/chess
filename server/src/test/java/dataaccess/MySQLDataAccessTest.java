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

}
