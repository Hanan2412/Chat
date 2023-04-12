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
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import NormalObjects.FileManager;
import NormalObjects.OnClick;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class GroupProfileAdapter extends RecyclerView.Adapter<GroupProfileAdapter.GroupViewHolder>{
    private List<User>recipients;
    private String currentUID;
    private FileManager fileManager;

    public interface onUserClick{
        void onImageClick(User user);
        void onEditBtnClick(User user);
    }

    private onUserClick listener;
    public GroupProfileAdapter()
    {
        recipients = new ArrayList<>();
        fileManager = FileManager.getInstance();
    }

    public void setListener(onUserClick listener)
    {
        this.listener = listener;
    }

    public void setCurrentUID(String UID)
    {
        currentUID = UID;
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
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_layout,parent,false);
      return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupProfileAdapter.GroupViewHolder holder, int position) {
        User user = recipients.get(position);
        String link = user.getPictureLink();
        if (link!=null && !link.isEmpty()) {
            Bitmap profileBitmap = fileManager.readImage(holder.itemView.getContext(), FileManager.user_profile_images, user.getUserUID());
            if (profileBitmap != null)
            {
                holder.profileImage.setImageBitmap(profileBitmap);
                holder.profileImage.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), android.R.color.transparent));
            }
        }
        if (!user.getUserUID().equals(currentUID))
        {
            holder.editImageBtn.setVisibility(View.GONE);
        }
        else
        {
            holder.editImageBtn.setVisibility(View.VISIBLE);
        }

    }

    public void updateUser(User user)
    {
        int index = findUserPosition(user.getUserUID());
        if (index > -1)
        {
            recipients.set(index, user);
            notifyItemChanged(index);
        }
    }

    public int findUserPosition(String uid)
    {
        for (int i = 0;i<recipients.size();i++)
        {
            if (recipients.get(i).getUserUID().equals(uid))
                return i;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        if (recipients == null)
            Log.e("GroupProfileAdapterError","recipients are null");
        return recipients.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder
    {

        ShapeableImageView profileImage;
        ImageButton editImageBtn;


        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            editImageBtn = itemView.findViewById(R.id.editBtn);
            editImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null)
                    {
                        listener.onEditBtnClick(recipients.get(getAdapterPosition()));
                    }
                }
            });
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null)
                    {
                        listener.onImageClick(recipients.get(getAdapterPosition()));
                    }
                }
            });
        }
    }
}
