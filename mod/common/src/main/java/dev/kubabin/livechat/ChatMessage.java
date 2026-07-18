package dev.kubabin.livechat;

public record ChatMessage(
        String player,
        String message,
        String timestamp
) {
    public String toJson() {
        return String.format("{\"player\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}", player, message, timestamp);
    }
}