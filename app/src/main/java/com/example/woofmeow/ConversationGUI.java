package com.example.woofmeow;

import java.util.ArrayList;

import NormalObjects.Message;
import NormalObjects.User;

public interface ConversationGUI {
    void onReceiveMessages(ArrayList<Message>messages);
    void onReceiveUser(User user);
    void onReceiveSingleMessage(Message message);
    void onReceiveItemChange(Message message,int position);
    void onRemoveDeletedMessage(int position);
}
