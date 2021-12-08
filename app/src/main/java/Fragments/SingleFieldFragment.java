package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.R;

@SuppressWarnings("Convert2Lambda")
public class SingleFieldFragment extends Fragment {

    public interface onName{
        void onGroupName(String name);
    }

    private onName listener;

    public void setListener(onName listener){this.listener = listener;}
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_field,container,false);
        EditText name = view.findViewById(R.id.name);
        Button done = view.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onGroupName(name.getText().toString());
            }
        });
        return view;
    }

}
