package client;

import com.google.gson.Gson;
import model.AuthData;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

/* see shared/src/main/server/ServerFacade.java in Petshop files for analogous code */

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private static final Gson GSON = new Gson();

    public ServerFacade(int port) {
        // serverUrl = url;
        serverUrl = "http://localhost:" + port;
    }

    /* standard form:
        method
        path
        body shape
        auth token presence
        return type
     */

    public AuthData register(String username, String password, String email) throws Exception {
        var body = Map.of("username", username, "password", password, "email", email);
        var request = buildRequest("POST", "/user", body, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public AuthData login(String username, String password) throw exception {

    }

    public void logout(authTokem) {

    }

    public

    // just HTTP helpers
    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeBody(body));
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        if (authToken != null) {
            builder.header("authorization", authToken);
        }
        return builder.build();
    }

    private BodyPublisher makeBody(Object body) {
        if (body != null) {
            return BodyPublishers.ofString(GSON.toJson(body));
        }
        return BodyPublishers.noBody();
    }



    private HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        return client.send(request, BodyHandlers.ofString());
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws Exception {
        /* var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status); */

        if (response.statusCode() != 200) {
            // GSON.fromJson(response.body(), Map.class);
            var error = GSON.fromJson(response.body(), Map.class);
            throw new Exception((String) error.get("message"));
        }
        if (responseClass == null) {
            return null;
        }
        return GSON.fromJson(response.body(), responseClass);
    }
}