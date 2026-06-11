package client.websocket;

import websocket.messages.ServerMessage;

// break this out b/c it has to handle more error types than petshop
public interface ServerMessageHandler {
    void handleServerMessage(ServerMessage message);
}