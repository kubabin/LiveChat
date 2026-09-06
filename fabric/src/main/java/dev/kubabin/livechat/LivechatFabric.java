package dev.kubabin.livechat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LivechatFabric implements ModInitializer {
    private final LivechatBase livechat = new LivechatBase();
    private static final UniversalLogger logger = new UniversalLogger() {
        private final Logger LOGGER = LoggerFactory.getLogger("livechat");
        @Override
        public void info(String message) {
            LOGGER.info(message);
        }

        @Override
        public void warn(String message) {
            LOGGER.warn(message);
        }

        @Override
        public void error(String message) {
            LOGGER.error(message);
        }
    };

    @Override
    public void onInitialize() {
        ConfigManager.load();
        FabricConfig config = ConfigManager.get();

        livechat.setGameDir(FabricLoader.getInstance().getGameDir());
        ServerPlayerEvents.JOIN.register(player -> livechat.onPlayerJoin(player.getScoreboardName()));
        ServerPlayerEvents.LEAVE.register(player -> livechat.onPlayerLeave(player.getScoreboardName()));
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            livechat.onPlayerChat(sender.getScoreboardName(), message.signedContent());
        });
        livechat.startup(config.host, config.port, logger);
    }

}
