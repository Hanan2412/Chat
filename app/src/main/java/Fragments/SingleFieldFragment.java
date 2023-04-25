package Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

@SuppressWarnings("Convert2Lambda")
public class SingleFieldFragment extends DialogFragment {

    public interface onText {
        void onTextChange(String name);
    }
    public interface onDismiss{
        void onDismissFragment();
    }
    private String fieldTxt = "Text";
    private onText listener;
    private onDismiss onDismissListener;
    private int inputType=-1;
    public void setHint(String txt)
    {
        fieldTxt = txt;
    }

    public void setListener(onText listener){this.listener = listener;}

    public void setOnDismissListener(onDismiss listener)
    {
        onDismissListener = listener;
    }

    public void setInputType(int inputType)
    {
        this.inputType = inputType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.single_field,null);
        TextInputEditText editText = view.findViewById(R.id.name);
        TextInputLayout textInputLayout = view.findViewById(R.id.textInputLayout);
        textInputLayout.setHint(fieldTxt);
        if (inputType!=-1)
            editText.setInputType(inputType);
        Button done = view.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText()!=null)
                    listener.onTextChange(editText.getText().toString());
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null)
            onDismissListener.onDismissFragment();
    }
}
