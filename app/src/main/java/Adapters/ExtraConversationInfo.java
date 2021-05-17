package Adapters;

import java.util.ArrayList;

public interface ExtraConversationInfo {
    void onRecipientStatusChange();
    void onMuteConversationStatusChange(ArrayList<String>muted);
}
