package Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.woofmeow.R;
import com.google.android.material.textfield.TextInputEditText;

@SuppressWarnings("Convert2Lambda")
public class SignFragment extends DialogFragment {

    public interface Sign{
        void onSign(String sign,String email,String password);
        void onCancel();
    }

    private Sign callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (Sign)context;
        }catch (ClassCastException e){
            throw new ClassCastException("activity must implement interface Sign");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.sign,null);
        final TextInputEditText email = view.findViewById(R.id.email);
        final TextInputEditText password = view.findViewById(R.id.password);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(email.getText() != null && password.getText()!=null && getArguments()!=null)
                {
                    if(password.getText().toString().length()<6)
                        Toast.makeText(requireContext(), "Password must be 6 characters or more", Toast.LENGTH_SHORT).show();
                    else
                        callback.onSign(getArguments().getString("Sign"),email.getText().toString(),password.getText().toString());
                }
            }
        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onCancel();
            }
        });
        builder.setView(view);
        return builder.create();
    }
}
