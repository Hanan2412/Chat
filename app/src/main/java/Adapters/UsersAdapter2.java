package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.woofmeow.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class UsersAdapter2 extends BaseAdapter {

    private List<User>users;


    public UsersAdapter2()
    {
        users = new ArrayList<>();
    }

    public void addUser(User user)
    {
        users.add(user);
        notifyDataSetChanged();
    }

    public void removeUser(int position)
    {
        users.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater!=null)
        {
            User user = users.get(position);
            if(convertView==null)
                convertView = inflater.inflate(R.layout.user_cell,parent,false);
            ShapeableImageView imageView = convertView.findViewById(R.id.userImage);
            TextView userName = convertView.findViewById(R.id.userName);
            Button groupTalk = convertView.findViewById(R.id.addUser);
            Button singleTalk = convertView.findViewById(R.id.individual);
            groupTalk.setVisibility(View.GONE);
            singleTalk.setVisibility(View.GONE);
            Button remove = convertView.findViewById(R.id.remove);
            remove.setVisibility(View.GONE);
            String name = user.getName() + " " + user.getLastName();
            userName.setText(name);
            Picasso.get().load(user.getPictureLink()).into(imageView);

        }
        return convertView;
    }

    public void clear()
    {
        users.clear();
        notifyDataSetChanged();
    }
}
