package dev.kubabin.livechat;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(LivechatNeoforge.MODID)
public class LivechatNeoforge
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "livechat";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LivechatBase livechat = new LivechatBase();
    public static class NeoforgeLogger implements UniversalLogger {
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
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public LivechatNeoforge(ModContainer modContainer) {
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (LivechatNeoforge) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        livechat.startup(Config.host, Config.port, new NeoforgeLogger());
    }
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        livechat.shutdown();
    }
    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        livechat.onPlayerChat(event.getPlayer().getName().getString(), event.getMessage().getString());
    }
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        livechat.onPlayerJoin(event.getEntity().getName().getString());
    }
    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        livechat.onPlayerLeave(event.getEntity().getName().getString());
    }

}
