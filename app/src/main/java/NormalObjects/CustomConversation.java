package NormalObjects;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;

public class CustomConversation extends ArrayList<Conversation> {

    private ArrayList<onConversationChange>listeners;

    public CustomConversation(int initialCapacity) {
        super(initialCapacity);
    }

    public CustomConversation() {
        super();
    }

    public CustomConversation(@NonNull Collection<? extends Conversation> c) {
        super(c);
    }

    private ArrayList<Conversation>conversations;
    public void setConversations(ArrayList<Conversation>conversations)
    {
        this.conversations = conversations;
        if(listeners!=null) {
            int i = 0;
            for (onConversationChange listener : listeners) {
                String lastMessage = conversations.get(i).getLastMessage();
//                String lastMessageTime = conversations.get(i).getLastMessageTime();
//                listener.onLastMessageInConversationChange(lastMessage, lastMessageTime);
            }
        }
    }

    public void addConversation(Conversation conversation)
    {
        if(conversations == null)
            conversations = new ArrayList<>();
        conversations.add(conversation);
        if(listeners!=null) {
            int i = 0;
            for (onConversationChange listener : listeners) {
                String lastMessage = conversation.getLastMessage();
//                String lastMessageTime = conversation.getLastMessageTime();
//                listener.onLastMessageInConversationChange(lastMessage, lastMessageTime);
            }
        }
    }

    public void setListeners(onConversationChange listener)
    {
        if(listeners==null)
            listeners = new ArrayList<>();
        listeners.add(listener);
    }
}
