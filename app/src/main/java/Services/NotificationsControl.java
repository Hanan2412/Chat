package Services;



import java.util.ArrayList;

import NormalObjects.Conversation;


public interface NotificationsControl {
    void onConversationMute(ArrayList<Conversation>conversations);
}
