package com.esb.cosmetiqueshop.model;

/** Message du chat assistant beauté (utilisateur ou IA). */
public class BeautyChatMessage {

    private final boolean fromUser;
    private final String text;
    private final long createdAt;

    public BeautyChatMessage(boolean fromUser, String text, long createdAt) {
        this.fromUser = fromUser;
        this.text = text;
        this.createdAt = createdAt;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public String getText() {
        return text;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
