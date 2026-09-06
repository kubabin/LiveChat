package dev.kubabin.livechat;

public record ChatMessage(
        String player,
        String message,
        String timestamp
) {
    public String toJson() {
        // Simple JSON "serialization", I'm too lazy to properly use GSON
        return String.format("{\"player\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                player,
                message.replace("\"", "\\\""),
                timestamp);
    }
}