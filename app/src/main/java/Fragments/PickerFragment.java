package Fragments;



import android.content.Context;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vanniktech.emoji.EmojiEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


@SuppressWarnings("Convert2Lambda")
public class PickerFragment extends Fragment {


    private SingleFieldFragment singleFieldFragment;
    private List<Integer>time;
    private List<Integer>date;
    public static PickerFragment newInstance() {

        Bundle args = new Bundle();
        PickerFragment fragment = new PickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface onPickerClick{
        void onPicked(List<Integer>time, List<Integer>date, String message);
        void onCancelPick();
    }

    onPickerClick callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            callback = (onPickerClick)context;
        }catch (ClassCastException e){
            throw new ClassCastException("Activity must implement interface onPickerClick");
        }
    }

    public PickerFragment() {
        singleFieldFragment = new SingleFieldFragment();
        time = new ArrayList<>();
        date = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delayed_message_fragment,container,false);
        singleFieldFragment.setHint(requireActivity().getResources().getString(R.string.message_hint));
        singleFieldFragment.setListener(new SingleFieldFragment.onText() {
            @Override
            public void onTextChange(String txt) {
                if(!txt.isEmpty())
                    callback.onPicked(time, date, txt);
                singleFieldFragment.dismiss();
                callback.onCancelPick();
            }
        });
        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setListener(new TimePickerFragment.onTimePicker() {
            @Override
            public void onDateTimeChosen(List<Integer> time, List<Integer> date) {
                PickerFragment.this.time = time;
                PickerFragment.this.date = date;
            }

            @Override
            public void onTimeChosen(List<Integer> time) {
                PickerFragment.this.time = time;
            }

            @Override
            public void onDateChosen(List<Integer> date) {
                PickerFragment.this.date = date;
            }
        });
        DelayedLocationFragment delayedLocationFragment = new DelayedLocationFragment();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.time)
                {
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.root_container123, timePickerFragment).commit();
                }
                else if (item.getItemId() == R.id.location)
                {
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.root_container123, delayedLocationFragment).commit();
                }
                else if (item.getItemId() == R.id.message)
                {

                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.time);
        Button sendDelayMessageBtn = view.findViewById(R.id.sendMessageBtn);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);
//        EmojiEditText editText = view.findViewById(R.id.messageText);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        sendDelayMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleFieldFragment.show(requireActivity().getSupportFragmentManager(),"SingleFieldFragment - Date");
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCancelPick();
            }
        });
        return view;
    }
}
