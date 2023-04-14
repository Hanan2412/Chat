package Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.woofmeow.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.image_layout,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        if (getArguments()!=null) {
            String image = getArguments().getString("image");
            ImageView imageView = view.findViewById(R.id.imagePreview);
            if (getArguments().getString("uri")!=null)
            {
                Picasso.get().load(new File(getArguments().getString("uri"))).into(imageView);
            }
            else
                Picasso.get().load(image).into(imageView);
        }
        return builder.setCancelable(true).setView(view).create();
    }
}
