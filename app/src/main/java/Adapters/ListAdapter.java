package Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.woofmeow.R;
import java.util.ArrayList;
import java.util.List;

import NormalObjects.Conversation;
import NormalObjects.FileManager;
import NormalObjects.User;

public class ListAdapter extends BaseAdapter {

    private ArrayList<User>users;
    private List<Conversation> conversations;
    public void setConversations(List<Conversation>conversations){
        this.conversations = conversations;
        notifyDataSetChanged();
    }
    public void setUsers(ArrayList<User>users){
        this.users = users;
    notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (users!=null)
            return users.size();
        else if (conversations!=null)
            return conversations.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        if (users!=null)
            return users.get(position);
        else return conversations.get(position);
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
            LinearLayout root = convertView.findViewById(R.id.rootLayout);
            TextView name = convertView.findViewById(R.id.name);
            TextView lastName = convertView.findViewById(R.id.lastName);
            ImageView profileImage = convertView.findViewById(R.id.profileImage);
            FileManager fm = FileManager.getInstance();
            if (conversations!=null)
            {
                Conversation conversation = conversations.get(position);
                switch (conversation.getConversationType())
                {
                    case single:
                        root.setBackground(ResourcesCompat.getDrawable(parent.getResources(),R.drawable.conversation_cell_not_selected,parent.getContext().getTheme()));
                        break;
                    case group:
                        root.setBackground(ResourcesCompat.getDrawable(parent.getResources(),R.drawable.conversation_group_cell_not_selected,parent.getContext().getTheme()));
                        break;
                    case sms:
                        root.setBackground(ResourcesCompat.getDrawable(parent.getResources(),R.drawable.conversation_sms_cell_not_selected,parent.getContext().getTheme()));
                        break;
                }
                name.setText(conversation.getGroupName());
                Bitmap bitmap = fm.readImage(parent.getContext(), FileManager.conversationProfileImage,conversation.getConversationID());
                if (bitmap!=null)
                    profileImage.setImageBitmap(bitmap);
                lastName.setText(conversation.getLastMessage());
            }
            else
            {
                User user = users.get(position);
                String fullName = user.getName() + " " + user.getLastName();
                name.setText(fullName);
                Bitmap bitmap = fm.readImage(parent.getContext(),FileManager.user_profile_images,user.getUserUID());
                if (bitmap!=null)
                    profileImage.setImageBitmap(bitmap);
                lastName.setVisibility(View.GONE);
            }
        }
        return convertView;
    }
}
