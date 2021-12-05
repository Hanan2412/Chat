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
public class UsersAdapter extends BaseAdapter {

    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<User>users;
    private List<Integer> selected;
    public UsersAdapter()
    {
        users = new ArrayList<>();
        selected = new ArrayList<>();
    }

    public interface startConversation{
        void onStart(User user);
        void onAddToGroup(User user);
        void onRemoveFromGroup(User user);
    }

    private startConversation listener;

    public void setListener(startConversation listener){this.listener = listener;}


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
            Button remove = convertView.findViewById(R.id.remove);
            ImageButton removeSelection = convertView.findViewById(R.id.removeBtn);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    users.remove(position);
                    notifyDataSetChanged();
                }
            });
            String name = user.getName() + " " + user.getLastName();
            userName.setText(name);
            Picasso.get().load(user.getPictureLink()).into(imageView);
            singleTalk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onStart(user);
                }
            });
            View finalConvertView = convertView;
            groupTalk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //remove.callOnClick();
                    removeSelection.setVisibility(View.VISIBLE);
                    groupTalk.setVisibility(View.GONE);
                    select(position, finalConvertView);
                    listener.onAddToGroup(user);
                }
            });
            removeSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSelection.setVisibility(View.GONE);
                    groupTalk.setVisibility(View.VISIBLE);
                    deSelect(position,finalConvertView);
                    listener.onRemoveFromGroup(user);
                }
            });
        }
        return convertView;
    }

    public void addUser(User user)
    {
        users.add(user);
        notifyDataSetChanged();
    }


    public void ClearData()
    {
        users.clear();
    }

    public void select(int position,View view)
    {
        selected.add(position);
        view.setBackgroundResource(R.drawable.background_gradient);
    }

    public void deSelect(int position,View view)
    {
        for(Integer p : selected)
        {
            if (p == position)
            {
                selected.remove(p);
                view.setBackgroundResource(R.drawable.background_gradient2);
                break;
            }
        }
    }

    public void changeSelection(int position, View view)
    {
        if (isSelected(position))
            deSelect(position,view);
        else
            select(position,view);
    }

    public boolean isSelected(int position)
    {
       return selected.contains(position);
    }

    public List<Integer> getSelected()
    {
        return selected;
    }
}
