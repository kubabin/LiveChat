package dev.kubabin.livechat;

public record ChatMessage(
        String player,
        String message,
        String timestamp
) { }