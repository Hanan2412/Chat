package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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
    private List<Integer> date;

    public TimePickerFragment() {
        time = new ArrayList<>();
        date = new ArrayList<>();
    }

    public interface onTimePicker{
        void onDateTimeChosen(List<Integer>time, List<Integer>date);
        void onTimeChosen(List<Integer>time);
        void onDateChosen(List<Integer>date);
    }

    private onTimePicker listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_picker, container, false);
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        DatePicker datePicker = view.findViewById(R.id.datePicker);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                time.clear();
                time.clear();
                time.add(hourOfDay);
                time.add(minute);
                if (listener!=null)
                {
                    listener.onTimeChosen(time);
                }
            }
        });
        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int day, int month, int year) {
                date.clear();
                date.add(day);
                date.add(month);
                date.add(year);
                if (listener!=null)
                {
                    listener.onDateChosen(date);
                }
            }
        });
        timePicker.setHour(Calendar.getInstance().get(Calendar.HOUR));
        Calendar calendar = Calendar.getInstance();
        datePicker.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        return view;
    }

    public void setListener(onTimePicker listener){
        this.listener = listener;
    }
}
