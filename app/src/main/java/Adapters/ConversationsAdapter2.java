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


import Consts.ConversationType;
import Consts.MessageType;
import NormalObjects.Conversation;
import NormalObjects.FileManager;
import NormalObjects.Message;
import Time.TimeFormat;

@SuppressWarnings("Convert2Lambda")
public class ConversationsAdapter2 extends RecyclerView.Adapter<ConversationsAdapter2.ConversationsViewHolder> {


    private final FileManager fileManager;

    public interface onPressed {
        void onLongPressed(boolean selected, Conversation conversation);

        void onClicked(Conversation conversation);

        void onImageDownloaded(Conversation conversation,boolean image);
    }

    private onPressed callback;

    public void setListener(onPressed listener) {
        callback = listener;
    }

    private ArrayList<Conversation> conversations;
    private ArrayList<String> selected = new ArrayList<>();
    private ArrayList<Integer> selectedPosition = new ArrayList<>();

    public ConversationsAdapter2()
    {
        conversations = new ArrayList<>();
        fileManager = FileManager.getInstance();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setConversations(ArrayList<Conversation> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    public void addConversation(Conversation conversation) {
        init();
        conversations.add(conversation);
        notifyItemInserted(conversations.size() - 1);
    }
    public void setConversation(Conversation conversation,int position)
    {
        init();
        if(conversation!=null) {
            conversations.add(position, conversation);
            notifyItemInserted(position);
            notifyItemRangeChanged(position, conversations.size());
        }
    }
    private void init()
    {
        if (conversations == null)
            conversations = new ArrayList<>();
    }
    public void updateConversation(Conversation conversation) {
            int index = findCorrectConversationIndex(conversation.getConversationID());
            if (index > -1) {
                if (!conversation.isTyping()){
                    if (!conversation.getLastMessageID().equals(conversations.get(index).getLastMessageID()))
                    {
                        conversations.remove(index);
                        conversations.add(0,conversation);
                        notifyItemMoved(index,0);
                    }
                    else {
                        conversations.set(index, conversation);
                        notifyItemChanged(index);
                    }
//                    conversation.setRecipient(conversations.get(index).getRecipient());
//                    conversations.remove(index);
//                    notifyItemRemoved(index);
//                    conversations.add(0,conversation);
//                    notifyItemInserted(0);
//                    notifyItemRangeChanged(0,conversations.size());
                }
            }

    }

    public void UpdateConversation(Message message,String conversationID)
    {
        int index = findCorrectConversationIndex(conversationID);
        Conversation conversation = conversations.get(index);
        conversation.setLastMessageTime(message.getArrivingTime());
        conversation.setLastMessage(message.getMessage());
        conversation.setRecipientName(message.getGroupName());
        notifyItemChanged(index);
    }

    public Conversation findConversation(String conversationID)
    {
        int index = findCorrectConversationIndex(conversationID);
        if(index>=0)
            return conversations.get(index);
        return null;
    }

    public void MuteConversation(String conversationID,boolean mute)
    {
        int index =  findCorrectConversationIndex(conversationID);
        Conversation conversation = conversations.get(index);
        conversation.setMuted(mute);
        notifyItemChanged(index);
    }



    public void DeleteConversation(String conversationID)
    {
        int index = findCorrectConversationIndex(conversationID);
        conversations.remove(index);
        notifyItemRemoved(index);
    }
    private int findCorrectConversationIndex(String conversationID) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversationID.equals(conversations.get(i).getConversationID()))
                return i;
        }
        return -1;
    }

    public void deleteConversation(Conversation conversation) {
        int index = findCorrectConversationIndex(conversation.getConversationID());
        if (index > -1) {
            conversations.remove(index);
            notifyItemRemoved(index);
        }
    }

    @NonNull
    @Override
    public ConversationsAdapter2.ConversationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConversationType type = ConversationType.values()[viewType];
        View view;
        switch (type)
        {
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
                Log.e("Error viewType","view type is not conversation Type, setting default value");
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversations_group_cell, parent, false);
        }
        return new ConversationsAdapter2.ConversationsViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void Reset() {
        conversations = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsAdapter2.ConversationsViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Conversation conversation = conversations.get(position);
        if (conversation.getMessageType() == MessageType.VoiceMessage.ordinal())
            holder.lastMessage.setText(R.string.voice_message);
        else
            holder.lastMessage.setText(conversation.getLastMessage());
        TimeFormat timeFormat = new TimeFormat();
        String lastMessageTime;
        if (conversation.getLastMessageTime() == null)
            conversation.setLastMessageTime(conversation.getLastMessageID());
        if (conversation.getLastMessageTime().contains("/"))
            lastMessageTime = conversation.getLastMessageTime();
        else
            lastMessageTime = timeFormat.getFormattedDate(Long.parseLong(conversation.getLastMessageTime()));
        holder.lastMessageTime.setText(lastMessageTime);
        holder.recipientName.setText(conversation.getGroupName());
        if (conversation.isMuted())
            holder.conversationStatus.setVisibility(View.VISIBLE);
        else
            holder.conversationStatus.setVisibility(View.GONE);
        Bitmap bitmap = fileManager.readImage(holder.itemView.getContext().getApplicationContext(),FileManager.conversationProfileImage,conversation.getConversationID());
        if (bitmap == null)
            if (conversation.getConversationType() == ConversationType.group)
                bitmap = fileManager.readImage(holder.itemView.getContext().getApplicationContext(),FileManager.conversationProfileImage,conversation.getConversationID());
            else if (conversation.getConversationType() == ConversationType.single)
                bitmap = fileManager.readImage(holder.itemView.getContext().getApplicationContext(),FileManager.user_profile_images,conversation.getRecipient());
        if (bitmap!=null)
            holder.profileImage.setImageBitmap(bitmap);
        else Log.e("bitmap - conversationsAdapter2", "no image for conversation");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClicked(conversations.get(position));
            }
        });
        holder.rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (selected.contains(conversation.getConversationID())) {
                    switch (conversation.getConversationType())
                    {
                        case single:
                            holder.rootLayout.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.conversation_cell_not_selected, holder.itemView.getContext().getTheme()));
                            break;
                        case sms:
                            holder.rootLayout.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.conversation_sms_cell_not_selected, holder.itemView.getContext().getTheme()));
                            break;
                        case group:
                            holder.rootLayout.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.conversation_group_cell_not_selected, holder.itemView.getContext().getTheme()));
                            break;
                    }
                    selected.remove(conversation.getConversationID());
                    callback.onLongPressed(false, conversation);
                    selectedPosition.remove((Integer) position);
                } else {
                    switch (conversation.getConversationType())
                    {
                        case single:
                            holder.rootLayout.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.conversation_cell_selected, holder.itemView.getContext().getTheme()));
                            break;
                        case sms:
                            holder.rootLayout.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.conversation_sms_cell_selected, holder.itemView.getContext().getTheme()));
                            break;
                        case group:
                            holder.rootLayout.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.conversation_group_cell_selected, holder.itemView.getContext().getTheme()));
                            break;
                    }

                    selected.add(conversation.getConversationID());
                    callback.onLongPressed(true, conversation);
                    selectedPosition.add(position);
                }
                return true;
            }
        });
        if(conversation.isMuted())
        {
            holder.conversationStatus.setVisibility(View.VISIBLE);
            holder.conversationStatus.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_baseline_volume_off_24,holder.itemView.getContext().getTheme()));
        }
        if(conversation.isBlocked())
        {
            holder.conversationStatus.setVisibility(View.VISIBLE);
            holder.conversationStatus.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_baseline_block_white,holder.itemView.getContext().getTheme()));
        }
        if(!conversation.isMuted() && !conversation.isBlocked())
        {
            holder.conversationStatus.setVisibility(View.GONE);
            holder.conversationStatus.setImageDrawable(null);
        }
        if (conversation.getUnreadMessages()!=0)
        {
            holder.unreadMessages.setVisibility(View.VISIBLE);
            holder.unreadMessages.setText(String.valueOf(conversation.getUnreadMessages()));
            Log.e("unread","unread should be visible");
        }
    }


    public void PinConversation(Conversation conversation) {
      int index = findCorrectConversationIndex(conversation.getConversationID());
      Conversation conversation1 = conversations.get(index);
      conversations.add(0,conversation1);
      conversations.remove(index);
      notifyItemMoved(index,0);
    }

    public void BlockConversation(boolean blocked,String conversationID)
    {
        int index =  findCorrectConversationIndex(conversationID);
        Conversation conversation = conversations.get(index);
        conversation.setBlocked(blocked);
        notifyItemChanged(index);
    }


    public ArrayList<Integer> getSelectedPosition() {
        return selectedPosition;
    }


    public Conversation getConversation(int position) {
        if(position>-1 && position < getItemCount())
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
        TextView lastMessageTime, recipientName,unreadMessages;
        ImageView conversationStatus;
        RelativeLayout rootLayout;

        public ConversationsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.conversationImage);
            lastMessage = itemView.findViewById(R.id.lastMessageSent);
            lastMessageTime = itemView.findViewById(R.id.timeLastMessage);
            recipientName = itemView.findViewById(R.id.recipientName);
            conversationStatus = itemView.findViewById(R.id.conversationStatus);
            unreadMessages = itemView.findViewById(R.id.unreadMessages);
            rootLayout = itemView.findViewById(R.id.conversationCell);
        }

    }

    public void Search(String search)
    {
        ArrayList<Conversation> conversationsCopy = new ArrayList<>();
        for(Conversation conversation : conversations)
        {
            if (conversation.getRecipientName()!=null)
                if(conversation.getRecipientName().toLowerCase().contains(search.toLowerCase()))
                {
                    conversationsCopy.add(conversation);
                }
        }
        conversations = conversationsCopy;
    }

    @Override
    public int getItemViewType(int position) {
        switch (conversations.get(position).getConversationType())
        {
            case sms:
                return ConversationType.sms.ordinal();
            case group:
                return ConversationType.group.ordinal();
            case single:
                return ConversationType.single.ordinal();
            default:
                Log.e("Conversation Type","non existing conversation type");
                return ConversationType.group.ordinal();
        }

    }
}
