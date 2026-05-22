package service;

import dataaccess.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private DataAccess dataAccess;
    private UserService userService;
    private ClearService clearService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        clearService = new ClearService(dataAccess);
    }

    @Test
    void registerSuccess() throws DataAccessException {
        var result = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        assertEquals("sam", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void clearSuccess() throws DataAccessException {
        userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        clearService.clear();
        assertNull(dataAccess.getUser("sam"));
    }
}