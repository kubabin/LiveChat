package dev.kubabin.livechat;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;

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

    public Livechat(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        try {
            httpServer = HttpServer.create(new java.net.InetSocketAddress(Config.socketHost, Config.socketPort), 0);
            httpServer.createContext("/chat", exchange -> {
                String response = gson.toJson(messageQueue);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
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
        // Timestamp in ISO 8601 format
        String timestamp = Clock.systemUTC().instant().toString();
        messageQueue.add(new ChatMessage(playerName, message, timestamp));
        // Limit the queue size to 100 messages
        if (messageQueue.size() > 100) {
            messageQueue.removeFirst();
        }
    }
}
