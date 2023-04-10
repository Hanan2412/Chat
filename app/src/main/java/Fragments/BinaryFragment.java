package Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.woofmeow.R;

@SuppressWarnings("Convert2Lambda")
public class BinaryFragment extends DialogFragment {


    private String f_btn_text;
    private String s_btn_text;

    public interface BinaryClickListener{
        void onFirstBtnClick();
        void onSecondBtnClick();
    }

    private BinaryClickListener listener;

    public void setListener(BinaryClickListener listener)
    {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.binary_layout,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        Button firstBtn = view.findViewById(R.id.firstBtn);
        if (f_btn_text != null)
            firstBtn.setText(f_btn_text);
        firstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null)
                    listener.onFirstBtnClick();
            }
        });

        Button secondBtn = view.findViewById(R.id.secondBtn);
        if (s_btn_text != null)
            secondBtn.setText(s_btn_text);
        secondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null)
                    listener.onSecondBtnClick();
            }
        });
        builder.setView(view);
        return builder.create();
    }

    public void setFirstBtnText(String name)
    {
        f_btn_text = name;
    }

    public void setSecondBtnText(String name)
    {
        s_btn_text = name;
    }
}
