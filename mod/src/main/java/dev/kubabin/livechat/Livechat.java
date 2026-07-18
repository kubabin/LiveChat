package dev.kubabin.livechat;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpExchange;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import com.sun.net.httpserver.HttpServer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.ArrayDeque;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Livechat.MODID)
public class Livechat {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "livechat";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    private HttpServer httpServer;
    private final ArrayDeque<ChatMessage> messageQueue = new ArrayDeque<>();
    private final Gson gson = new Gson();

    public Livechat(ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    public void addMessage(String player, String message) {
        String timestamp = Clock.systemUTC().instant().toString();
        messageQueue.add(new ChatMessage(player, message, timestamp));
        // Limit the queue size
        if (messageQueue.size() > Config.MAX_MESSAGES.get()) {
            messageQueue.removeFirst();
        }
    }
    private void chatEndpoint(HttpExchange exchange) throws IOException {
        String response = gson.toJson(messageQueue);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // Allow CORS for all origins
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        try {
            httpServer = HttpServer.create(new java.net.InetSocketAddress(Config.socketHost, Config.socketPort), 0);
            httpServer.createContext("/chat", this::chatEndpoint);
            httpServer.createContext("/api/chat", this::chatEndpoint);
            // Serve other static files if needed
            httpServer.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) {
                    path = "/index.html";
                }
                InputStream is = Livechat.class.getResourceAsStream("/assets/livechat/web" + path);
                LOGGER.info("Serving static file: {}", path);
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
            httpServer.start();
            LOGGER.info("Server socket started on port {}", Config.socketPort);
        } catch (Exception e) {
            LOGGER.error("Failed to start server socket on port {}", Config.socketPort, e);
        }
    }

    // on server shutdown, stop the HTTP server
    @SubscribeEvent
    public void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        if (httpServer != null) {
            httpServer.stop(0);
            LOGGER.info("Server socket stopped");
        }
    }
    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        // Add the chat message to the queue
        String playerName = event.getPlayer().getName().getString();
        String message = event.getMessage().getString();
        addMessage(playerName, message);
    }
    @SubscribeEvent
    public void onPlayerJoin(PlayerLoggedInEvent event) {
        // Add a join message to the queue
        String playerName = event.getEntity().getName().getString();
        String message = playerName + " joined the game";
        addMessage(playerName, message);
    }
    @SubscribeEvent
    public void onPlayerLeave(PlayerLoggedOutEvent event) {
        // Add a leave message to the queue
        String playerName = event.getEntity().getName().getString();
        String message = playerName + " left the game";
        addMessage(playerName, message);
    }
}
