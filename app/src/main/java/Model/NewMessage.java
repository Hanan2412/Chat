package Model;

public interface NewMessage {
    boolean isConversationExists(String conversationID);

    boolean isNotificationsAllowed();

    boolean isConversationBlocked(String conversationID);

    boolean isUserBlocked(String userID);

    boolean isConversationOpen(String conversationID);

    boolean isConversationMuted(String conversationID);

    boolean isUserMuted(String userID);

}
