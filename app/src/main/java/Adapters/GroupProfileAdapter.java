package Adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.R;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import NormalObjects.FileManager;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class GroupProfileAdapter extends RecyclerView.Adapter<GroupProfileAdapter.GroupViewHolder>{
    public interface onUserInteraction{
        void onMute(String userID);
        void onBlock(String userID);
    }
    public onUserInteraction callback;
    public void setListener(onUserInteraction listener){callback = listener;}
    private List<User>recipients;

    public GroupProfileAdapter()
    {
        recipients = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRecipients(List<User>recipients)
    {
        this.recipients = recipients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupProfileAdapter.GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_activity2,parent,false);
      return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupProfileAdapter.GroupViewHolder holder, int position) {
        User user = recipients.get(position);
        String[] userDits = {user.getName(),user.getLastName()};
        List<String>ditsList = new ArrayList<>(Arrays.asList(userDits));
        if (user.getPhoneNumber()!=null)
            ditsList.add(user.getPhoneNumber());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(), android.R.layout.simple_list_item_1, ditsList);
        holder.listView.setAdapter(adapter);
        holder.muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onMute(user.getUserUID());
            }
        });
        holder.blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBlock(user.getUserUID());
            }
        });
        FileManager fm = FileManager.getInstance();
        Bitmap bitmap = fm.readImage(holder.itemView.getContext(),FileManager.user_profile_images,user.getUserUID());
        if (bitmap!=null)
            holder.profilePic.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        if (recipients == null)
            Log.e("GroupProfileAdapterError","recipients are null");
        return recipients.size();
    }


    public class GroupViewHolder extends RecyclerView.ViewHolder
    {
        ListView listView;
        ImageView profilePic;
        ImageButton muteBtn,blockBtn;
        AppBarLayout appBarLayout;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            listView = itemView.findViewById(R.id.userDetails);
            profilePic = itemView.findViewById(R.id.profileImage);
//            muteBtn = itemView.findViewById(R.id.mute);
            blockBtn = itemView.findViewById(R.id.block);
            appBarLayout = itemView.findViewById(R.id.appbarLayout);
            appBarLayout.setVisibility(View.GONE);
        }
    }
}
