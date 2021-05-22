package Adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.example.woofmeow.MainActivity;
import com.example.woofmeow.R;
import com.google.android.material.imageview.ShapeableImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


import Consts.MessageType;
import NormalObjects.Conversation;
import NormalObjects.CustomConversation;
import NormalObjects.onConversationChange;
@Deprecated
public class ConversationsAdapter extends BaseAdapter {

    //private ArrayList<Chat>list;
    private ArrayList<Conversation>conversations;
    private CustomConversation customConversation;
    public ConversationsAdapter()
    {
        conversations = new ArrayList<>();
        customConversation = new CustomConversation();
    }


    public void setConversations(ArrayList<Conversation>conversations){
        this.conversations = conversations;
        customConversation.setConversations(conversations);
    }

    public void addConversation(Conversation conversation)
    {
        conversations.add(conversation);
        //customConversation.addConversation(conversation);
    }

    public void updateConversation(Conversation conversation)
    {
        int index = FindCorrectConversation(conversation.getConversationID());
        if(index!=-1)
        {
            conversations.set(index,conversation);
            notifyDataSetChanged();
        }
    }

    public void deleteConversation(Conversation conversation)
    {
        int index = FindCorrectConversation(conversation.getConversationID());
        if(index!=-1)
        {
            conversations.remove(index);
            notifyDataSetChanged();
        }
    }

    private int FindCorrectConversation(String conversationID)
    {
       for(int i = 0;i<conversations.size();i++)
       {
           if(conversations.get(i).getConversationID().equals(conversationID))
               return i ;
       }
       return -1;
    }


    @Override
    public int getCount() {
        if(conversations!=null)
            return conversations.size();
        else
            return 0;
    }


    @Override
    public Object getItem(int position) {
        return conversations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(layoutInflater!=null)
        {
            Conversation conversation = conversations.get(position);



            if(convertView == null)
                 convertView = layoutInflater.inflate(R.layout.conversations_cell,parent,false);
            ShapeableImageView imageView = convertView.findViewById(R.id.conversationImage);
            TextView lastMessage = convertView.findViewById(R.id.lastMessageSent);
            TextView lastTime = convertView.findViewById(R.id.timeLastMessage);
            TextView recipientName = convertView.findViewById(R.id.recipientName);
            ImageView conversationStatus = convertView.findViewById(R.id.conversationStatus);


            if(conversation.getMessageType() == MessageType.VoiceMessage.ordinal())
            {
                lastMessage.setText(R.string.voice_message);
            }
            else
                lastMessage.setText(conversation.getLastMessage());


            lastTime.setText(conversation.getLastMessageTime());
            if(conversation.isMuted())
                conversationStatus.setVisibility(View.VISIBLE);
            else
                conversationStatus.setVisibility(View.GONE);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + conversation.getRecipient() + "/pictureLink");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String pictureLink = snapshot.getValue(String.class);
                    Picasso.get().load(pictureLink).into(imageView);
                    reference.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            });

            DatabaseReference nameReference = FirebaseDatabase.getInstance().getReference("users/" + conversation.getRecipient() + "/name");
            nameReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String recipientNameString = snapshot.getValue(String.class);
                    recipientName.setText(recipientNameString);
                    nameReference.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            });


            ImageView statusView = convertView.findViewById(R.id.statusView);
            DatabaseReference statusReference = FirebaseDatabase.getInstance().getReference("users/" + conversation.getRecipient() + "/status");
            statusReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String status =  snapshot.getValue(String.class);
                    if(status!=null) {
                        if(status.equals(MainActivity.ONLINE_S))
                            statusView.setBackground(ResourcesCompat.getDrawable(parent.getResources(),R.drawable.circle_green,parent.getContext().getTheme()));
                        else if(status.equals(MainActivity.OFFLINE_S) || status.equals(MainActivity.STANDBY_S))
                            statusView.setBackground(ResourcesCompat.getDrawable(parent.getResources(),R.drawable.circle_red,parent.getContext().getTheme()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            });
            return convertView;
        }
        return null;
    }



}
