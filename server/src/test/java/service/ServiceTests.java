package service;

import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*; // jupiter API

public class ServiceTests {
    private DataAccess dataAccess;
    private UserService userService;

    void setUp() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
    }

    void registerSuccess() throws DataAccessException {
        var result = userService.register(new UserService.RegisterRequest("sam", "pass", "sam@mail.com"));
        assertEquals("sam", result.username());
        assertNotNull(result.authToken());
    }
}