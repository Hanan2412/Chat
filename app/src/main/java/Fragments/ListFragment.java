package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.R;
import Adapters.ListAdapter2;
import NormalObjects.OnClick;

@SuppressWarnings("Convert2Lambda")
public class ListFragment extends Fragment {

    private ListAdapter2 adapter;
//    private OnClick listener;

    public interface ItemClickListener{
        void onClickItem(int position);
    }
    private ItemClickListener listener;
    public ListFragment() {
    }

    public void setListener(ItemClickListener listener)
    {
        this.listener = listener;
    }

    public ListAdapter2 getAdapter()
    {
        return adapter;
    }

    public void setAdapter(ListAdapter2 adapter)
    {
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list, container, false);
        ListView listView = view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (listener!=null)
                {
                    listener.onClickItem(i);
                }
            }
        });
        return view;
    }
}
