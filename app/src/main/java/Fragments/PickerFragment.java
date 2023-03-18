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


    private int[] time;
    private List<Integer>time1;
    private List<Integer>date;
    public static PickerFragment newInstance() {

        Bundle args = new Bundle();
        PickerFragment fragment = new PickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface onPickerClick{
        void onPicked(int[] time,String text);
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

    private PickerFragment() {
        super();
        time = new int[5];//number of fields
        Arrays.fill(time, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delayed_message_fragment,container,false);
        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setListener(new onFragmentData() {
            @Override
            public void onDataList(List<Integer> list) {
                time1 = list;
            }
        });
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setListener(new onFragmentData() {
            @Override
            public void onDataList(List<Integer> list) {
                date = list;
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
                else if (item.getItemId() == R.id.date)
                {
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.root_container123, datePickerFragment).commit();
                }
                else if (item.getItemId() == R.id.location)
                {
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.root_container123, delayedLocationFragment).commit();
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.location);
        Button sendDelayMessageBtn = view.findViewById(R.id.sendDelayMessageBtn);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);
        EmojiEditText editText = view.findViewById(R.id.messageText);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        sendDelayMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (date!=null)
                    for (int i = 0; i < 2; i++)
                    {
                        time[i] = date.get(i);
                    }
                if (time1!=null)
                {
                    int j = 3;
                    for (int i = 0; i < time1.size(); i++)
                    {
                        time[j] = time1.get(i);
                        j++;
                    }
                }
                //in case user didn't set one or more fields, auto set fields to current value in calendar
                if(time[0] == -1)
                    time[0] = Calendar.getInstance().get(Calendar.YEAR);
                if(time[1] == -1)
                    time[1] = Calendar.getInstance().get(Calendar.MONTH);
                if(time[2] == -1)
                    time[2] = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                if(time[3] == -1)
                    time[3] = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if(time[4] == -1)
                    time[4] = Calendar.getInstance().get(Calendar.MINUTE);
                if (editText.getText()!=null)
                {
                    String message = editText.getText().toString();
                    if (!message.matches("")) {
                        callback.onPicked(time, message);
                        callback.onCancelPick();
                    }
                    else
                        Toast.makeText(requireContext(), "can't send an empty message", Toast.LENGTH_SHORT).show();
                }

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
