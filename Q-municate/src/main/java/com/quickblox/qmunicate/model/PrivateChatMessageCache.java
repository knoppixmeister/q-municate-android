package com.quickblox.qmunicate.model;

public class PrivateChatMessageCache {

    private String message;
    private int senderId;
    private int chatId;
    private String attachUrl;

    public PrivateChatMessageCache(String message, int senderId, int chatId, String attachUrl) {
        this.message = message;
        this.senderId = senderId;
        this.chatId = chatId;
        this.attachUrl = attachUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getAttachUrl() {
        return attachUrl;
    }

    public void setAttachUrl(String attachUrl) {
        this.attachUrl = attachUrl;
    }
}