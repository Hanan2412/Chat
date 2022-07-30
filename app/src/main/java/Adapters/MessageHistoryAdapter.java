package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.woofmeow.R;

import java.util.List;

import NormalObjects.Message;
import NormalObjects.MessageHistory;

public class MessageHistoryAdapter extends BaseAdapter {

    private List<MessageHistory>messages;

    public MessageHistoryAdapter(List<MessageHistory>messages)
    {
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater!=null) {
            convertView = layoutInflater.inflate(R.layout.message_history, parent, false);
            TextView date = convertView.findViewById(R.id.messagesDate);
            TextView text = convertView.findViewById(R.id.message);
            ImageView imageView = convertView.findViewById(R.id.seenBy);
            MessageHistory message = messages.get(position);
            date.setText(message.getSendingTime());
            text.setText(message.getMessage());
        }
        return convertView;
    }
}
