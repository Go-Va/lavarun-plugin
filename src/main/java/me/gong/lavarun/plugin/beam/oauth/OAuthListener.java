package me.gong.lavarun.plugin.beam.oauth;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.beam.BeamManager;
import me.gong.lavarun.plugin.util.JSONUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;

import java.net.InetSocketAddress;

public class OAuthListener extends WebSocketServer {

    public OAuthListener(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        try {
            JSONObject obj = (JSONObject) JSONUtils.parser.parse(s);
            AuthResponse r = AuthResponse.fetchFrom(obj);

            if(r != null) InManager.get().getInstance(BeamManager.class).getOAuthManager().onResponse(r);
        } catch (Exception e) {
            e.printStackTrace();
            //not using json
            webSocket.close();
            InManager.get().getInstance(BeamManager.class).getOAuthManager().handleError(e);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }
}
