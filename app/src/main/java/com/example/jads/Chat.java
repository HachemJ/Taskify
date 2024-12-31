package com.example.jads;
import java.util.Map;

public class Chat {
    private String chatId;
    private String otherUserId; // ID of the user this chat is with
    private String otherUserName; // First name + Last name
    private Map<String, Boolean> participants; // Map of participant user IDs

    public Chat() {
    }

    public Chat(String chatId, String otherUserId, String otherUserName, Map<String, Boolean> participants) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.participants = participants;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public Map<String, Boolean> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, Boolean> participants) {
        this.participants = participants;
    }

    /**
     * Get the ID of the other participant in the chat.
     *
     * @param currentUserId The ID of the current user.
     * @return The ID of the other participant.
     */
    public String getOtherUserId(String currentUserId) {
        if (participants != null) {
            for (String participantId : participants.keySet()) {
                if (!participantId.equals(currentUserId)) {
                    return participantId;
                }
            }
        }
        return null; // Return null if no other user is found
    }
}
