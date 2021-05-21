package Adapters;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


import Consts.MessageType;
import NormalObjects.Conversation;

@SuppressWarnings("Convert2Lambda")
public class ConversationsAdapter2 extends RecyclerView.Adapter<ConversationsAdapter2.ConversationsViewHolder> {


    private ArrayList<String> recipientsNames = new ArrayList<>();

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

    public void setConversations(ArrayList<Conversation> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    public void addConversation(Conversation conversation) {
        if (conversations == null)
            conversations = new ArrayList<>();
        conversations.add(conversation);
        notifyItemInserted(conversations.size() - 1);
    }

    public void updateConversation(Conversation conversation) {
            int index = FindCorrectConversationIndex(conversation.getConversationID());
            if (index > -1) {
                //places the conversation at the beginning of the list
                if (!conversation.getLastMessageID().equals(conversations.get(index).getLastMessage()) && !conversation.getLastMessage().equals(conversations.get(index).getLastMessage())) {
                    //this if prevents the update if the only update is the typing indicator
                    if (index == 0)
                    {
                        conversations.set(index, conversation);
                        notifyItemChanged(index);
                    }
                    else {
                        conversations.remove(index);
                        notifyItemRemoved(index);
                        conversations.add(0, conversation);
                        notifyItemInserted(0);
                    }
                    /*conversations.remove(index + 1);//causes flickering
                    notifyItemRemoved(index + 1);
                    //conversations.set(index, conversation);
           /*if (getItemCount() > 1)
                notifyItemRangeInserted(1,getItemCount());*/
                    //notifyItemChanged(index);
                }
            }

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

    public void SetBackUp() {
        backUp = new ArrayList<>();
        backUp.addAll(conversations);
    }

    public void Reset() {
        conversations = backUp;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsAdapter2.ConversationsViewHolder holder, int position) {

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

        /*if (recipientsNames.size() > position)
            holder.recipientName.setText(recipientsNames.get(position));*/

        SharedPreferences savedImagesPreferences = holder.itemView.getContext().getSharedPreferences("SavedImages",Context.MODE_PRIVATE);
        //image exists in the app
        if (savedImagesPreferences.getBoolean(conversation.getRecipient(),false))
        {
            try {
                ContextWrapper contextWrapper = new ContextWrapper(holder.itemView.getContext().getApplicationContext());
                File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                File imageFile = new File(directory,conversation.getRecipient() + "_Image");
                Bitmap imageBitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                holder.profileImage.setImageBitmap(imageBitmap);
                conversation.setRecipientImagePath(imageFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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
                            ContextWrapper contextWrapper = new ContextWrapper(holder.itemView.getContext().getApplicationContext());
                            File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                            if (!directory.exists())
                                if (!directory.mkdir()) {
                                    Log.e("error", "couldn't create a directory in conversationAdapter2");
                                }
                            File Path = new File(directory, conversation.getRecipient() + "_Image");
                            conversations.get(position).setRecipientImagePath(Path.getAbsolutePath());
                            callback.onImageDownloaded(conversations.get(position),true);
                            try {
                                FileOutputStream fileOutputStream = new FileOutputStream(Path);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                fileOutputStream.close();
                                SharedPreferences.Editor editor = savedImagesPreferences.edit();
                                editor.putBoolean(conversation.getRecipient(),true);
                                editor.apply();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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

        /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + conversation.getRecipient() + "/pictureLink");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pictureLink = snapshot.getValue(String.class);
                Picasso.get().load(pictureLink).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        ContextWrapper contextWrapper = new ContextWrapper(holder.itemView.getContext().getApplicationContext());
                        File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                        if (!directory.exists())
                            if (!directory.mkdir()) {
                                Log.e("error", "couldn't create a directory in conversationAdapter2");
                            }
                        File Path = new File(directory, conversation.getRecipient() + "_Image");
                        conversations.get(position).setRecipientImagePath(Path.getAbsolutePath());
                        callback.onImageDownloaded(conversations.get(position),true);
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(Path);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
        });*/

       /* DatabaseReference nameReference = FirebaseDatabase.getInstance().getReference("users/" + conversation.getRecipient() + "/name");
        nameReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String recipientNameString = snapshot.getValue(String.class);
                holder.recipientName.setText(recipientNameString);
                nameReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });*/

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
                /*Intent intent = new Intent(holder.itemView.getContext(), ConversationActivity.class);
                intent.putExtra("conversationID", conversations.get(position).getConversationID());
                intent.putExtra("recipient", conversations.get(position).getRecipient());
                intent.putExtra("recipientPhone", conversations.get(position).getRecipientPhoneNumber());
                holder.itemView.getContext().startActivity(intent);*/
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
    }

    public void PinConversation(Conversation conversation) {
      /*int index = FindCorrectConversationIndex(conversation.getConversationID());
      Conversation conversation1 = conversations.get(0);
      conversations.set(0,conversation);
      conversations.set(index,conversation1);*/
    }

    /*public void setRecipientName(String recipientName) {
        recipientsNames.add(recipientName);
    }*/




    public ArrayList<Integer> getSelectedPosition() {
        return selectedPosition;
    }


    public Conversation getConversation(int position) {
        return conversations.get(position);
    }


    @Override
    public int getItemCount() {
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

}
