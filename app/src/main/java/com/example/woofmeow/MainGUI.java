package com.example.woofmeow;

import java.util.ArrayList;

import NormalObjects.Conversation;
import NormalObjects.User;

public interface MainGUI {
    void onReceiveUser(User user);
    void onReceiveConversations(ArrayList<Conversation> conversations);
    void onReceiveConversation(Conversation conversation);
    void onChangedConversation(Conversation conversation);
    void onRemoveConversation(Conversation conversation);
    void onReceiveUsersQuery(User user);

    void onVersionChange(float newVersionNumber);
}
