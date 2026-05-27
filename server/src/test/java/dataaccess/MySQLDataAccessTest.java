package dataaccess;

import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

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
}
