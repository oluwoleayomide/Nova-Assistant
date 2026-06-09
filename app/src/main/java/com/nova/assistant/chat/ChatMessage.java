package com.nova.assistant.chat;

public class ChatMessage {

    public static final int ROLE_USER = 0;
    public static final int ROLE_NOVA = 1;

    private final String text;
    private final int role;
    private final long timestamp;

    public ChatMessage(String text, int role) {
        this.text      = text;
        this.role      = role;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText()      { return text; }
    public int    getRole()      { return role; }
    public long   getTimestamp() { return timestamp; }

    public boolean isUser() { return role == ROLE_USER; }
    public boolean isNova() { return role == ROLE_NOVA; }

    public String getFormattedTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}
