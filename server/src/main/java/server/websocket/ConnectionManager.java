package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Integer, Set<String>> gameUsers = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Integer> userToGame = new ConcurrentHashMap<>();

    public void add(int gameID, String username, Session session) {
        sessions.put(username, session);

        // couldn't figure out lambda function
        if (!gameUsers.containsKey(gameID)) {
            gameUsers.put(gameID, ConcurrentHashMap.newKeySet());
        }
        gameUsers.get(gameID).add(username);
        userToGame.put(username, gameID);
    }



    public void remove(String username) {
        Integer gameID = userToGame.remove(username);
        if (gameID != null) {
            Set<String> users = gameUsers.get(gameID);
            if (users != null) {

                // function calls itself
                users.remove(username);
            }
        }
        sessions.remove(username);
    }


    public void broadcast(int gameID, String jsonMessage) throws Exception {
        sendToGame(gameID, null, jsonMessage);
    }

    public void broadcastExcluding(int gameID, String excludeUser, String jsonMessage) throws Exception {
        sendToGame(gameID, excludeUser, jsonMessage);
    }

    public void sendToUser(String username, String jsonMessage) throws Exception {
        Session session = sessions.get(username);
        if (session != null && session.isOpen()) {
            session.getRemote().sendString(jsonMessage);
        }
    }


    private void sendToGame(int gameID, String excludeUser, String jsonMessage) throws Exception {
        Set<String> users = gameUsers.get(gameID);
        if (users == null) {
            return;
        }
        for (String username : users) {
            if (username.equals(excludeUser)) {
                continue;
            }
            Session session = sessions.get(username);
            if (session != null && session.isOpen()) {
                session.getRemote().sendString(jsonMessage);
            }
        }
    }
}
