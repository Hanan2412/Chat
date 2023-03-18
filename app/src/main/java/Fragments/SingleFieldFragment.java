package Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.R;

@SuppressWarnings("Convert2Lambda")
public class SingleFieldFragment extends DialogFragment {

    public interface onText {
        void onTextChange(String name);
    }
    private String fieldTxt = "Text";
    private onText listener;

    public void setHint(String txt)
    {
        fieldTxt = txt;
    }

    public void setListener(onText listener){this.listener = listener;}
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.single_field,container,false);
//        EditText name = view.findViewById(R.id.name);
//        name.setHint(fieldTxt);
//        Button done = view.findViewById(R.id.done);
//        done.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.onTextChange(name.getText().toString());
//            }
//        });
//        return view;
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.single_field,null);
        EditText name = view.findViewById(R.id.name);
        name.setHint(fieldTxt);
        Button done = view.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTextChange(name.getText().toString());
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);
        return builder.create();
    }
}
