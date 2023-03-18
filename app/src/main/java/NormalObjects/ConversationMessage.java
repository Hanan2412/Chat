package NormalObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConversationMessage implements Serializable {

    private String conversationID;
    private String recipientsIds;
    private String removedRecipient;
    private String addedRecipient;
    private int conversationMessageKind;
    private String conversationName;

    public ConversationMessage(String conversationID) {
        this.conversationID = conversationID;

    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getRecipientsId() {
        return recipientsIds;
    }

    public void setRecipientsId(String recipientsIds) {
        this.recipientsIds = recipientsIds;
    }

    public String getRemovedRecipient() {
        return removedRecipient;
    }

    public void setRemovedRecipient(String removedRecipient) {
        this.removedRecipient = removedRecipient;
    }

    public String getAddedRecipient() {
        return addedRecipient;
    }

    public void setAddedRecipient(String addedRecipient) {
        this.addedRecipient = addedRecipient;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public int getConversationMessageKind() {
        return conversationMessageKind;
    }

    public void setConversationMessageKind(int conversationMessageKind) {
        this.conversationMessageKind = conversationMessageKind;
    }
}
