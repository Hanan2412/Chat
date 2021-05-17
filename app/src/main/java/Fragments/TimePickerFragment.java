package Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.R;

@SuppressWarnings("Convert2Lambda")
public class TimePickerFragment extends Fragment {

    private String startTime,endTime,startDate,endDate;
    public static TimePickerFragment getInstance(int start)
    {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("page",start);
        timePickerFragment.setArguments(bundle);
        return timePickerFragment;
    }

    public interface onTimePicked{
        void onLetsMeet(String start, String end);
        void onCancel();
    }

    private onTimePicked callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (onTimePicked)context;
        }catch (ClassCastException e){
            throw new ClassCastException("Context/Activity must implement interface onTimePicked");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_picker_fragment,container,false);
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        TextView title = view.findViewById(R.id.title);
        title.setVisibility(View.GONE);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);
        Button LetsMeetBtn = view.findViewById(R.id.sendDelayMessageBtn);
        LetsMeetBtn.setText("Lets Meet");
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                String time = hourOfDay + ":" + minute;
                Bundle bundle = getArguments();
                if (bundle!=null) {
                    int start = bundle.getInt("page");
                    if (start == 0)
                        startTime = time;
                    else if (start == 1)
                        endTime = time;
                }
            }
        });
        DatePicker datePicker = view.findViewById(R.id.datePicker);
        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String date = dayOfMonth + "/" + monthOfYear + "/" + year;
                Bundle bundle = getArguments();
                if (bundle!=null) {
                    int start = bundle.getInt("page");
                    if (start == 0)
                       startDate = date;
                    else if (start == 1)
                        endDate = date;
                }
            }
        });
        datePicker.setMinDate(System.currentTimeMillis());
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCancel();
            }
        });
        LetsMeetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Once",Context.MODE_PRIVATE);
                if (!sharedPreferences.getBoolean("meetUp",false))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Notice")
                            .setMessage("A meetUp will only be set if the other party will agree to it")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("meetUp",true);
                                    editor.apply();
                                    String start = startDate + " " + startTime;
                                    String end = endDate + " " + endTime;
                                    callback.onLetsMeet(start,end);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            callback.onCancel();
                        }
                    }).create().show();
                }
                else {
                    String start = startDate + " " + startTime;
                    String end = endDate + " " + endTime;
                    callback.onLetsMeet(start, end);
                }
            }
        });
        return view;
    }
}
