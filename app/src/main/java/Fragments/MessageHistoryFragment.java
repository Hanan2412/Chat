package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.R;
import java.util.List;

import Adapters.MessageHistoryAdapter;
import NormalObjects.Message;
import NormalObjects.MessageHistory;

public class MessageHistoryFragment extends Fragment {

    private MessageHistoryAdapter adapter;
//
//    public MessageHistoryFragment(List<MessageHistory> messageHistory) {
//        adapter = new MessageHistoryAdapter(messageHistory);
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list, container, false);
        ListView listView = view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        return view;
    }
}
