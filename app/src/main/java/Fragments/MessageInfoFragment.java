package Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.woofmeow.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import Consts.ConversationType;
import NormalObjects.Message;
import Time.TimeFormat;

public class MessageInfoFragment extends Fragment {

    private Message message;

    public MessageInfoFragment(Message message) {
        this.message = message;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list,container,false);
        ListView listView = view.findViewById(android.R.id.list);
        TimeFormat timeFormat = new TimeFormat();
        String time = timeFormat.getFormattedTime(message.getSendingTime());
        String[] messageArray = {message.getContent(),message.getSenderName(),time,message.getArrivingTime()+"",message.getReadingTime() + ""};
        List<String> list = new ArrayList<>(Arrays.asList(messageArray));
        list.removeIf(Objects::isNull);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        return view;
    }
}
