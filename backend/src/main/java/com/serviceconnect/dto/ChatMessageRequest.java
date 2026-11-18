package com.serviceconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageRequest {
    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotBlank(message = "Sender name is required")
    private String senderName;

    @NotBlank(message = "Message text is required")
    private String message;

    public ChatMessageRequest() {}

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
