package dev.kubabin.livechat;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LivechatEventsListener implements Listener {
    private final LivechatBase livechat;
    public LivechatEventsListener(LivechatBase livechat) {
        this.livechat = livechat;
    }
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String playerName = event.getPlayer().getName();
        // Get the message as a raw string, without any formatting or colors
        String message = ((TextComponent) event.message()).content();
        livechat.onPlayerChat(playerName, message);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        livechat.onPlayerJoin(playerName);
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        livechat.onPlayerLeave(playerName);
    }
}
