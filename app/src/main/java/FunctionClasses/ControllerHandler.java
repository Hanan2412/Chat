package FunctionClasses;
//NOT FINISHED
public class ControllerHandler {

    private final String Users = "users";
    private final String Conversations = "conversations";
    private final String ConversationInfo = "conversationInfo";
    private final String ConversationMessage = "conversationMessages";
    private final String Slash = "/";

    public ControllerHandler()
    {

    }

    public String CreatePathForMessage(String userID,String conversationID)
    {
        return Users + Slash
                + userID + Slash
                + Conversations + Slash
                + conversationID + Slash
                + ConversationInfo + Slash
                + ConversationMessage;
    }

    public String CreatePathForConversation(String userID,String conversationID)
    {
        return Users + Slash
                + userID + Slash
                + Conversations + Slash
                + conversationID + Slash
                + ConversationInfo;
    }

    public String CreatePathForUser(String userID)
    {
        return Users + Slash
                + userID;
    }
}
