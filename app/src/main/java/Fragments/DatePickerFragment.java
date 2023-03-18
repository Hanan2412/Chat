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
public class DatePickerFragment extends Fragment {

    private List<Integer> time;
    private onFragmentData listener;

    public DatePickerFragment() {
        time = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.date_picker, container, false);
        DatePicker datePicker = view.findViewById(R.id.datePicker);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                time.clear();
                time.add(year);
                time.add(monthOfYear);
                time.add(dayOfMonth);
                if (listener!=null)
                {
                    listener.onDataList(time);
                }
            }
        });
        return view;
    }

    public void setListener(onFragmentData listener)
    {
        this.listener = listener;
    }
}
