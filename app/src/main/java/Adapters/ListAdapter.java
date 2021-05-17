package Adapters;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.woofmeow.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import NormalObjects.User;

public class ListAdapter extends BaseAdapter {

    private ArrayList<User>users;

    public void setUsers(ArrayList<User>users){this.users = users;}
    public void addUser(User user){
        if (users == null)
            users = new ArrayList<>();
        users.add(user);
       // notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater!=null) {
            convertView = layoutInflater.inflate(R.layout.list_adapter_view, parent, false);
            TextView name = convertView.findViewById(R.id.userName);
            TextView lastName = convertView.findViewById(R.id.userLastName);
            ImageView profileImage = convertView.findViewById(R.id.profileImage);
            User user = users.get(position);
            name.setText(user.getName());
            lastName.setText(user.getLastName());
            Bitmap userImage = LoadUserImageBitmap(position,parent);
            if (userImage == null)
                Picasso.get().load(user.getPictureLink()).into(profileImage);
            else
                profileImage.setImageBitmap(userImage);
        }
        return convertView;
    }

    private Bitmap LoadUserImageBitmap(int position,ViewGroup parent)
    {
        SharedPreferences savedImagesPreferences = parent.getContext().getSharedPreferences("SavedImages",Context.MODE_PRIVATE);
        User user = users.get(position);
        String sender = user.getUserUID();
        if (savedImagesPreferences.getBoolean(sender,false))
        {
            try {
                ContextWrapper contextWrapper = new ContextWrapper(parent.getContext().getApplicationContext());
                File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                File imageFile = new File(directory,sender + "_Image");
                return BitmapFactory.decodeStream(new FileInputStream(imageFile));

                //holder.profileImage.setImageBitmap(imageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
