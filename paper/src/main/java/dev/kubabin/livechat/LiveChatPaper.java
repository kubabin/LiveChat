package dev.kubabin.livechat;

import org.bukkit.plugin.java.JavaPlugin;

public final class LiveChatPaper extends JavaPlugin {

    private final LivechatBase livechat = new LivechatBase();
    public class PaperLogger implements UniversalLogger {
        @Override
        public void info(String message) {
            getLogger().info(message);
        }

        @Override
        public void warn(String message) {
            getLogger().warning(message);
        }

        @Override
        public void error(String message) {
            getLogger().severe(message);
        }
    }
    @Override
    public void onEnable() {
        // Log something to test
        getLogger().info("LiveChatPaper plugin enabled!");
        // Plugin startup logic
        saveDefaultConfig();
        livechat.startup(getConfig().getString("http.host"), getConfig().getInt("http.port"), new PaperLogger());
        getServer().getPluginManager().registerEvents(new LivechatEventsListener(livechat), this);
    }

    @Override
    public void onDisable() {
        livechat.shutdown();
    }
}
