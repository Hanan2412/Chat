package Adapters;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class NewGroupChatAdapter extends RecyclerView.Adapter<NewGroupChatAdapter.GroupViewHolder> {

    public interface onItemTouchListener{
        void onItemClick(int position);
    }
    private onItemTouchListener callback;
    public void setListener(onItemTouchListener listener){callback = listener;}
    private List<User>users;

    public NewGroupChatAdapter()
    {
        users = new ArrayList<>();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setUsers(List<User>users)
    {
        this.users = users;
        notifyDataSetChanged();
    }
    public List<User>getUsers()
    {
        return users;
    }

    @NonNull
    @Override
    public NewGroupChatAdapter.GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_pic,parent,false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewGroupChatAdapter.GroupViewHolder holder, int position) {
        Picasso.get().load(users.get(position).getPictureLink()).into(holder.profileImage);
        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder
    {
        ShapeableImageView profileImage;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
    public void addUser(User user)
    {
        users.add(user);
        notifyItemInserted(users.size()-1);
    }

    public void removeUser(int position)
    {
        users.remove(position);
        notifyItemRemoved(position);
    }

    public User getItem(int position)
    {
        return users.get(position);
    }

    public boolean isUserExists(String uid)
    {
        for (User user : users)
        {
            if (user.getUserUID().equals(uid))
                return true;
        }
        return false;
    }
}
