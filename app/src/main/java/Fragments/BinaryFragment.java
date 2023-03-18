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

public class BinaryFragment extends DialogFragment {

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
        Button openGalleryBtn = view.findViewById(R.id.openGalleryBtn);
        openGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null)
                    listener.onFirstBtnClick();
            }
        });

        Button openCameraBtn = view.findViewById(R.id.openCameraBtn);
        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null)
                    listener.onSecondBtnClick();
            }
        });
        builder.setView(view);
        return builder.create();
    }
}
