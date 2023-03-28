package Adapters;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;


import Consts.ConversationType;
import Consts.MessageType;
import NormalObjects.Conversation;
import NormalObjects.FileManager;

@SuppressWarnings("Convert2Lambda")
public class ConversationsAdapter2 extends RecyclerView.Adapter<ConversationsAdapter2.ConversationsViewHolder> {


    private final FileManager fileManager;
    private final String CONVERSATIONS_ADAPTER = "CONVERSATIONS_ADAPTER";

    public interface onPressed {
        void onClicked(Conversation conversation);

    }

    public interface onLongPress {
        void onConversationLongPress(Conversation conversation);
    }

    private onLongPress longPressListener;

    public void setLongPressListener(onLongPress listener) {
        longPressListener = listener;
    }

    private onPressed callback;

    public void setListener(onPressed listener) {
        callback = listener;
    }

    private List<Conversation> conversations;
    private List<Conversation> tmpConversations;

    public ConversationsAdapter2() {
        Log.d(CONVERSATIONS_ADAPTER, "constructor");
        init();
        fileManager = FileManager.getInstance();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    public void addConversation(Conversation conversation) {
        Log.d(CONVERSATIONS_ADAPTER, "add conversation: " + conversation.getConversationName());
        init();
        conversations.add(conversation);
        notifyItemInserted(conversations.size() - 1);
    }

    private void init() {
        Log.d(CONVERSATIONS_ADAPTER, "init");
        if (conversations == null)
            conversations = new ArrayList<>();
    }

    public void updateConversationSimple(Conversation conversation)
    {
        int index = findCorrectConversationIndex(conversation.getConversationID());
        conversations.set(index, conversation);
        notifyItemChanged(index);
    }

    public void updateConversation2(Conversation conversation)
    {
        Log.d(CONVERSATIONS_ADAPTER, "update conversation2");
        int index = findCorrectConversationIndex(conversation.getConversationID());
        if (index > -1)
        {
            if (conversation.isPinned())
            {
                moveToTop(conversation, index);
            }
            else if (conversation.getLastMessageID() != getConversation(index).getLastMessageID())
            {
                moveToTop(conversation, index);
            }
            else
            {
                conversations.set(index, conversation);
                notifyItemChanged(index);
            }
        }
        else if (conversations.isEmpty())
        {
            addConversation(conversation);
        }
        else {
            conversations.add(0,conversation);
            notifyItemRangeChanged(0, conversations.size());
            notifyItemInserted(0);
        }
    }

    public synchronized void updateConversation(Conversation conversation) {
        Log.d(CONVERSATIONS_ADAPTER, "update conversation: " + conversation.getConversationName());
        int index = findCorrectConversationIndex(conversation.getConversationID());
        if (index > -1) {
            if (!conversation.isTyping()) {
                if (conversation.getLastMessageID() != conversations.get(index).getLastMessageID())
                    moveToTop(conversation,index);

            }
        } else {
            if (conversations.isEmpty())
                addConversation(conversation);
            else {
                conversations.add(0, conversation);
                notifyItemRangeChanged(0, conversations.size());
                notifyItemInserted(0);
            }
        }

    }

    private void moveToTop(Conversation conversation, int index)
    {
        if (conversation.isPinned() && index == 0)
        {
            conversations.set(0, conversation);
            notifyItemChanged(0);
        }
        else {
            int startIndex = 0;
            if (getConversation(0).isPinned())
                startIndex = 1;
            notifyItemChanged(index);
            conversations.remove(index);
            conversations.add(startIndex, conversation);
            notifyItemMoved(index, startIndex);
        }
    }

    public void muteConversation(String conversationID, boolean mute) {
        Log.d(CONVERSATIONS_ADAPTER, "mute conversation: " + conversationID + " mute: " + mute);
        int index = findCorrectConversationIndex(conversationID);
        Conversation conversation = conversations.get(index);
        conversation.setMuted(mute);
        notifyItemChanged(index);
    }

    public void muteConversation(Conversation conversation)
    {
        int index = findCorrectConversationIndex(conversation.getConversationID());
        notifyItemChanged(index);
    }

    public void blockConversation(Conversation conversation)
    {
        int index = findCorrectConversationIndex(conversation.getConversationID());
        notifyItemChanged(index);
    }

    public void blockConversation(boolean blocked, String conversationID) {
        Log.d(CONVERSATIONS_ADAPTER, "block conversation: " + conversationID + " block: " + blocked);
        int index = findCorrectConversationIndex(conversationID);
        Conversation conversation = conversations.get(index);
        conversation.setBlocked(blocked);
        notifyItemChanged(index);
    }

    public void deleteConversation(String conversationID) {
        Log.d(CONVERSATIONS_ADAPTER, "delete conversation: " + conversationID);
        int index = findCorrectConversationIndex(conversationID);
        conversations.remove(index);
        notifyItemRemoved(index);
    }

    public Conversation findConversation(String conversationID) {
        Log.d(CONVERSATIONS_ADAPTER, "find conversation: " + conversationID);
        int index = findCorrectConversationIndex(conversationID);
        if (index >= 0)
            return conversations.get(index);
        return null;
    }

    public int findCorrectConversationIndex(String conversationID) {
        Log.d(CONVERSATIONS_ADAPTER, "find conversation index: " + conversationID);
        for (int i = 0; i < conversations.size(); i++) {
            if (conversationID.equals(conversations.get(i).getConversationID()))
                return i;
        }
        return -1;
    }

    @NonNull
    @Override
    public ConversationsAdapter2.ConversationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConversationType type = ConversationType.values()[viewType];
        View view;
        switch (type) {
            case sms:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversations_sms_cell, parent, false);
                break;
            case group:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversations_group_cell, parent, false);
                break;
            case single:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversations_single_cell, parent, false);
                break;
            default:
                Log.e("Error viewType", "view type is not conversation Type, setting default value");
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversations_group_cell, parent, false);
        }
        return new ConversationsAdapter2.ConversationsViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void reset() {
        conversations = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsAdapter2.ConversationsViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Conversation conversation = conversations.get(position);
        if (conversation.isPinned())
            holder.pinLayout.setVisibility(View.VISIBLE);
        else
            holder.pinLayout.setVisibility(View.GONE);
        if (conversation.getMessageType() == MessageType.voiceMessage.ordinal())
        {
            holder.lastMessage.setText(R.string.voice_message);
            holder.lastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_mic_black, 0 ,0,0);
        }
        else
            holder.lastMessage.setText(conversation.getLastMessage());
        if (conversation.getMessageType() == MessageType.imageMessage.ordinal() || conversation.getMessageType() == MessageType.photoMessage.ordinal())
        {
            holder.lastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_insert_photo_white, 0,0,0);
        }
        String lastMessageTime = conversation.getLastMessageTimeParse();
        holder.lastMessageTime.setText(lastMessageTime);
        holder.conversationName.setText(conversation.getConversationName());
        if (conversation.isMuted())
            holder.conversationStatus.setVisibility(View.VISIBLE);
        else
            holder.conversationStatus.setVisibility(View.GONE);
        Bitmap bitmap = fileManager.readImage(holder.itemView.getContext().getApplicationContext(), FileManager.conversationProfileImage, conversation.getConversationID());
        if (bitmap == null)
            if (conversation.getConversationType() == ConversationType.group.ordinal())
                bitmap = fileManager.readImage(holder.itemView.getContext().getApplicationContext(), FileManager.conversationProfileImage, conversation.getConversationID());
            else if (conversation.getConversationType() == ConversationType.single.ordinal())
                bitmap = fileManager.readImage(holder.itemView.getContext().getApplicationContext(), FileManager.user_profile_images, conversation.getRecipient());
        if (bitmap != null)
            holder.profileImage.setImageBitmap(bitmap);
        else Log.e("bitmap - conversationsAdapter2", "no image for conversation");

        if (conversation.isMuted()) {
            holder.conversationStatus.setVisibility(View.VISIBLE);
            holder.conversationStatus.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_baseline_volume_off_24, holder.itemView.getContext().getTheme()));
        }
        if (conversation.isBlocked()) {
            holder.conversationStatus.setVisibility(View.VISIBLE);
            holder.conversationStatus.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_baseline_block_white, holder.itemView.getContext().getTheme()));
        }
        if (!conversation.isMuted() && !conversation.isBlocked()) {
            holder.conversationStatus.setVisibility(View.GONE);
            holder.conversationStatus.setImageDrawable(null);
        }
        if (conversation.getUnreadMessages() != 0) {
            holder.unreadMessages.setVisibility(View.VISIBLE);
            holder.unreadMessages.setText(String.valueOf(conversation.getUnreadMessages()));
            Log.e("unread", "unread should be visible");
        }
    }

    public void pinConversation(Conversation conversation, boolean pin) {
        conversation.setPinned(pin);
        int conversationIndex = findCorrectConversationIndex(conversation.getConversationID());
        if (pin)
        {
            moveToTop(conversation, conversationIndex);
        }
        else
        {
            notifyItemChanged(conversationIndex);
        }

    }

    public Conversation getConversation(int position) {
        if (position > -1 && position < getItemCount())
            return conversations.get(position);
        else return null;
    }


    @Override
    public int getItemCount() {
        if (conversations == null)
            conversations = new ArrayList<>();
        return conversations.size();
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class ConversationsViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profileImage;
        EmojiTextView lastMessage;
        TextView lastMessageTime, conversationName, unreadMessages;
        ImageView conversationStatus;
        RelativeLayout rootLayout, pinLayout;

        public ConversationsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.conversationImage);
            lastMessage = itemView.findViewById(R.id.lastMessageSent);
            lastMessageTime = itemView.findViewById(R.id.timeLastMessage);
            conversationName = itemView.findViewById(R.id.recipientName);
            conversationStatus = itemView.findViewById(R.id.conversationStatus);
            unreadMessages = itemView.findViewById(R.id.unreadMessages);
            rootLayout = itemView.findViewById(R.id.conversationCell);
            pinLayout = itemView.findViewById(R.id.pinLayout);
        }

        public void bind(Conversation conversation) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) {
                        callback.onClicked(conversation);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longPressListener != null)
                        longPressListener.onConversationLongPress(conversation);
                    view.setSelected(!view.isSelected());
                    return true;
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        holder.bind(conversations.get(position));
    }

    public void searchForConversations(String searchQuery1)
    {
        if (tmpConversations != null)
            if (!tmpConversations.isEmpty())
                setConversations(tmpConversations);
        if (!searchQuery1.matches("^$"))
        {
            String searchQuery = searchQuery1.toLowerCase();
            List<Conversation> searchedConversations = new ArrayList<>();
            for (Conversation conversation : conversations) {
                if (conversation.getConversationName().toLowerCase().contains(searchQuery) || searchQuery.contains(conversation.getConversationName().toLowerCase())) {
                    searchedConversations.add(conversation);
                } else if (conversation.getLastMessage().toLowerCase().contains(searchQuery) || searchQuery.contains(conversation.getLastMessage().toLowerCase())) {
                    searchedConversations.add(conversation);
                }
            }
            tmpConversations = conversations;
            setConversations(searchedConversations);
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (ConversationType.values()[conversations.get(position).getConversationType()]) {
            case sms:
                return ConversationType.sms.ordinal();
            case group:
                return ConversationType.group.ordinal();
            case single:
                return ConversationType.single.ordinal();
            default:
                Log.e("Conversation Type", "non existing conversation type");
                return ConversationType.group.ordinal();
        }

    }
}
