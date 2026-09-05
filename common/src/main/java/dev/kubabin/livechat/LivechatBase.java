package dev.kubabin.livechat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;

public class LivechatBase {
    private HttpServer httpServer;
    private final ArrayDeque<ChatMessage> messageQueue = new ArrayDeque<>();
    private UniversalLogger logger;

    public void startup(String host, int port, UniversalLogger logger) {
        this.logger = logger;
        try {
            httpServer = HttpServer.create(new java.net.InetSocketAddress(host, port), 0);
            httpServer.createContext("/chat", this::chatEndpoint);
            httpServer.createContext("/api/chat", this::chatEndpoint);
            httpServer.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) {
                    path = "/index.html";
                }
                InputStream is = LivechatBase.class.getResourceAsStream(path);
                logger.info("Serving static file: " + path);
                if (is == null) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }
                byte[] response = is.readAllBytes();
                //exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });
            httpServer.setExecutor(null); // creates a default executor
            httpServer.start();
            logger.info("HTTP server started on " + host + ":" + port);
        } catch (Exception e) {
            logger.error("Failed to start HTTP server: " + e.getMessage());
        }
    }
    private void chatEndpoint(HttpExchange exchange) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("[");
        for (ChatMessage msg : messageQueue) {
            response.append(msg.toJson()).append(",");
        }
        if (!messageQueue.isEmpty()) {
            response.setLength(response.length() - 1); // Remove the trailing comma
        }
        response.append("]");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // Allow CORS for all origins
        exchange.sendResponseHeaders(200, response.toString().getBytes().length);
        exchange.getResponseBody().write(response.toString().getBytes());
        exchange.close();
    }
    public void addMessage(String player, String message) {
        String timestamp = java.time.Clock.systemUTC().instant().toString();
        messageQueue.add(new ChatMessage(player, message, timestamp));
        // Limit the queue size
        if (messageQueue.size() > 100) { // Example limit
            messageQueue.removeFirst();
        }
    }
    public void onPlayerChat(String player, String message) {
        addMessage(player, message);
    }
    public void onPlayerJoin(String player) {
        addMessage(player, player+" joined the game");
    }
    public void onPlayerLeave(String player) {
        addMessage(player, player+" left the game");
    }
    public void shutdown() {
        if (httpServer != null) {
            httpServer.stop(0);
            logger.info("HTTP server stopped");
        }
    }
}
