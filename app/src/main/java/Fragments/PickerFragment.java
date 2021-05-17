package Fragments;



import android.content.Context;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.R;

import java.util.Arrays;
import java.util.Calendar;


@SuppressWarnings("Convert2Lambda")
public class PickerFragment extends Fragment {


    private int[] time;

    public static PickerFragment newInstance() {

        Bundle args = new Bundle();
        PickerFragment fragment = new PickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface onPickerClick{
        void onPicked(int[] time);
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
        View view = inflater.inflate(R.layout.time_picker_fragment,container,false);
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        DatePicker datePicker = view.findViewById(R.id.datePicker);
        Button sendDelayMessageBtn = view.findViewById(R.id.sendDelayMessageBtn);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);

        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            time[3] = hourOfDay;
            time[4] = minute;
            }
        });
        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                time[0] = year;
                time[1] = monthOfYear;
                time[3] = dayOfMonth;
            }
        });

        datePicker.setMinDate(System.currentTimeMillis() - 1000);

        sendDelayMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                callback.onPicked(time);
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
