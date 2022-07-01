package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.woofmeow.R;
import java.util.ArrayList;
import java.util.List;


public class UserListAdapter extends BaseAdapter {

    private List<String>details;

    public UserListAdapter()
    {
        details = new ArrayList<>();
    }
    public void setDetails(List<String> details)
    {
        this.details = details;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return details.size();
    }

    @Override
    public Object getItem(int position) {
        return details.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(layoutInflater!=null) {
            if (convertView == null)
                convertView = layoutInflater.inflate(R.layout.user_cell2, parent, false);
            TextView userDetails = convertView.findViewById(R.id.userDetails);
            userDetails.setText(details.get(position));
        }
        return convertView;
    }
}
