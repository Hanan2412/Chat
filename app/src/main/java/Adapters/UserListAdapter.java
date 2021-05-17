package Adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.woofmeow.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import NormalObjects.User;

public class UserListAdapter extends BaseAdapter {

    public interface userListInterface{
        void onStartConversation(User user);
        void onViewProfile(User user);
    }

    private userListInterface callback;
    private ArrayList<User>users;
    public void addUser(User user){users.add(user);}
    public UserListAdapter(){users = new ArrayList<>();}
    public void setList(ArrayList<User>users){
    this.users = users;
    }
    public void setListener(userListInterface listener){callback = listener;}
    public void ClearData(){users.clear();};
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
        if(layoutInflater!=null){
            if(convertView == null)
                convertView = layoutInflater.inflate(R.layout.user_cell2,parent,false);
            TextView name = convertView.findViewById(R.id.userName);
            LinearLayout slideDownLayout = convertView.findViewById(R.id.slideDownLayout);
            Animation in = AnimationUtils.loadAnimation(parent.getContext(),android.R.anim.slide_in_left);
            Animation out = AnimationUtils.loadAnimation(parent.getContext(),android.R.anim.slide_out_right);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(slideDownLayout.getVisibility() == View.GONE) {
                        slideDownLayout.setVisibility(View.VISIBLE);
                        slideDownLayout.startAnimation(in);

                    }else
                    {
                        slideDownLayout.startAnimation(out);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                slideDownLayout.setVisibility(View.GONE);
                            }
                        },500);

                    }
                }
            });

            String userInfo = users.get(position).getName() + " " + users.get(position).getLastName();
            Button viewProfileBtn = convertView.findViewById(R.id.profile);
            Button sendMessageBtn = convertView.findViewById(R.id.message);
            sendMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onStartConversation(users.get(position));
                }
            });
            viewProfileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onViewProfile(users.get(position));
                }
            });
            name.setText(userInfo);
            ShapeableImageView userImage = convertView.findViewById(R.id.userImage);
            Picasso.get().load(users.get(position).getPictureLink()).into(userImage);

            return convertView;
        }
        return null;
    }
}
