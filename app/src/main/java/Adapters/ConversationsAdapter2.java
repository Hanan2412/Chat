package Adapters;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

import com.example.woofmeow.MainActivity;
import com.example.woofmeow.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;


import Consts.MessageType;
import NormalObjects.Conversation;
import NormalObjects.FileManager;
import NormalObjects.Message;

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
    private ArrayList<Conversation> backUp;
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
            notifyItemRangeChanged(0, conversations.size());
           /* int index = FindCorrectConversationIndex(conversation.getConversationID());
            if (index == -1) {
                conversations.add(position, conversation);
                notifyItemInserted(position);
                notifyItemRangeChanged(0, conversations.size());
            } else {
                conversations.remove(index);
                conversations.add(0, conversation);
                notifyItemMoved(index, 0);
            }*/
        }
    }
    private void init()
    {
        if (conversations == null)
            conversations = new ArrayList<>();
    }
    public void updateConversation(Conversation conversation) {
            int index = FindCorrectConversationIndex(conversation.getConversationID());
            if (index > -1) {
                //places the conversation at the beginning of the list
                if (!conversation.isTyping()){
                //if (!conversation.getLastMessageID().equals(conversations.get(index).getLastMessage()) && !conversation.getLastMessage().equals(conversations.get(index).getLastMessage())) {
                    //this if prevents the update if the only update is the typing indicator
                    conversation.setRecipient(conversations.get(index).getRecipient());
                    conversations.remove(index);
                    notifyItemRemoved(index);
                    conversations.add(0,conversation);
                    notifyItemInserted(0);
                    notifyItemRangeChanged(0,conversations.size());
                    //notifyItemMoved(index,0);
                }
            }

    }

    public void UpdateConversation(Message message,String conversationID)
    {
        int index = FindCorrectConversationIndex(conversationID);
        Conversation conversation = conversations.get(index);
        conversation.setLastMessageTime(message.getArrivingTime());
        conversation.setLastMessage(message.getMessage());
        conversation.setRecipientName(message.getRecipientName());
        notifyItemChanged(index);
    }

    public Conversation findConversation(String conversationID)
    {
        int index = FindCorrectConversationIndex(conversationID);
        return conversations.get(index);
    }

    public void MuteConversation(String conversationID,boolean mute)
    {
        int index =  FindCorrectConversationIndex(conversationID);
        Conversation conversation = conversations.get(index);
        conversation.setMuted(mute);
        notifyItemChanged(index);
    }



    public void DeleteConversation(String conversationID)
    {
        int index = FindCorrectConversationIndex(conversationID);
        conversations.remove(index);
        notifyItemRemoved(index);
    }
    private int FindCorrectConversationIndex(String conversationID) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversationID.equals(conversations.get(i).getConversationID()))
                return i;
        }
        return -1;
    }

    public void deleteConversation(Conversation conversation) {
        int index = FindCorrectConversationIndex(conversation.getConversationID());
        if (index > -1) {
            conversations.remove(index);
            notifyItemRemoved(index);
        }
    }

    @NonNull
    @Override
    public ConversationsAdapter2.ConversationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversations_cell, parent, false);
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
        holder.lastMessageTime.setText(conversation.getLastMessageTime());
        holder.recipientName.setText(conversation.getSenderName());
        if (conversation.isMuted())
            holder.conversationStatus.setVisibility(View.VISIBLE);
        else
            holder.conversationStatus.setVisibility(View.GONE);
        holder.recipientName.setText(conversation.getRecipientName());
        Bitmap bitmap = fileManager.getSavedImage(holder.itemView.getContext().getApplicationContext(), conversation.getRecipient() + "_Image");
        if (bitmap!=null)
        {
            holder.profileImage.setImageBitmap(bitmap);
            String path = fileManager.getSavedImagePath(holder.itemView.getContext().getApplicationContext(), conversation.getRecipient() + "_Image");
            conversation.setRecipientImagePath(path);
        }
        else
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + conversation.getRecipient() + "/pictureLink");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String pictureLink = snapshot.getValue(String.class);
                    Picasso.get().load(pictureLink).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            String path = fileManager.SaveUserImage(bitmap,conversation.getRecipient(),holder.itemView.getContext().getApplicationContext());
                            conversations.get(position).setRecipientImagePath(path);
                            callback.onImageDownloaded(conversations.get(position),true);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.e("Error","couldn't load image");
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
                    Picasso.get().load(pictureLink).into(holder.profileImage);
                    reference.removeEventListener(this);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            });
        }
        DatabaseReference statusReference = FirebaseDatabase.getInstance().getReference("users/" + conversation.getRecipient() + "/status");
        statusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status != null) {
                    if (status.equals(MainActivity.ONLINE_S))

                        holder.statusView.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.circle_green, holder.itemView.getContext().getTheme()));
                    else if (status.equals(MainActivity.OFFLINE_S) || status.equals(MainActivity.STANDBY_S))
                        holder.statusView.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.circle_red, holder.itemView.getContext().getTheme()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });
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
                    holder.rootLayout.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.conversation_cell_not_selected, holder.itemView.getContext().getTheme()));
                    selected.remove(conversation.getConversationID());
                    callback.onLongPressed(false, conversation);
                    selectedPosition.remove((Integer) position);
                } else {
                    holder.rootLayout.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.conversation_cell_selected, holder.itemView.getContext().getTheme()));
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
    }


    public void PinConversation(Conversation conversation) {
      int index = FindCorrectConversationIndex(conversation.getConversationID());
      Conversation conversation1 = conversations.get(index);
      conversations.add(0,conversation1);
      conversations.remove(index);
      notifyItemMoved(index,0);
    }

    /*public void setRecipientName(String recipientName) {
        recipientsNames.add(recipientName);
    }*/

    public void BlockConversation(boolean blocked,String conversationID)
    {
        int index =  FindCorrectConversationIndex(conversationID);
        Conversation conversation = conversations.get(index);
        conversation.setBlocked(blocked);
        notifyItemChanged(index);
    }


    public ArrayList<Integer> getSelectedPosition() {
        return selectedPosition;
    }


    public Conversation getConversation(int position) {
        return conversations.get(position);
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
        TextView lastMessage, lastMessageTime, recipientName;
        ImageView conversationStatus, statusView;
        RelativeLayout rootLayout;

        public ConversationsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.conversationImage);
            lastMessage = itemView.findViewById(R.id.lastMessageSent);
            lastMessageTime = itemView.findViewById(R.id.timeLastMessage);
            recipientName = itemView.findViewById(R.id.recipientName);
            conversationStatus = itemView.findViewById(R.id.conversationStatus);
            statusView = itemView.findViewById(R.id.statusView);
            rootLayout = itemView.findViewById(R.id.conversationCell);
        }

    }

    public void Search(String search)
    {
        ArrayList<Conversation> conversationsCopy = new ArrayList<>();
        for(Conversation conversation : conversations)
        {
            if(conversation.getRecipientName().toLowerCase().contains(search.toLowerCase()))
            {
                conversationsCopy.add(conversation);
            }
        }
        conversations = conversationsCopy;
    }
}
