package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("ALL")
public class TimePickerFragment extends Fragment {

    private List<Integer> time;
    private onFragmentData listener;

    public TimePickerFragment() {
        time = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_picker, container, false);
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                time.clear();
                time.add(hourOfDay);
                time.add(minute);
                if (listener!=null)
                {
                    listener.onDataList(time);
                }
            }
        });
        timePicker.setHour(Calendar.getInstance().get(Calendar.HOUR));
        return view;
    }

    public void setListener(onFragmentData listener)
    {
        this.listener = listener;
    }
}
