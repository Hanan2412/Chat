package Fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.woofmeow.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import Consts.ButtonType;

@SuppressWarnings("Convert2Lambda")
public class BottomSheetFragment extends BottomSheetDialogFragment {


    public static BottomSheetFragment newInstance() {

        Bundle args = new Bundle();
        BottomSheetFragment fragment = new BottomSheetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private BottomSheetFragment() {
        super();
    }

    public interface onSheetClicked {
        void onBottomSheetClick(ButtonType buttonType);
    }

    private onSheetClicked callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (onSheetClicked) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement onSheetClicked interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        FloatingActionButton locationFab = view.findViewById(R.id.locationFab);
        FloatingActionButton cameraFab = view.findViewById(R.id.cameraFab);
        FloatingActionButton galleryFab = view.findViewById(R.id.galleryFab);
        FloatingActionButton delayFab = view.findViewById(R.id.delayFab);
        FloatingActionButton contactFab = view.findViewById(R.id.contactFab);
        FloatingActionButton joke = view.findViewById(R.id.chuckBtn);
        locationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.location);
            }
        });
        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.camera);
            }
        });
        galleryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.gallery);
            }
        });
        delayFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.delay);
            }
        });
        contactFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.contact);
            }
        });
        joke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.joke);
            }
        });
        /*TextView attachFile = view.findViewById(R.id.attachFile);
        TextView attachLocation = view.findViewById(R.id.attachLocation);
        TextView attachCameraPhoto = view.findViewById(R.id.attachCamera);
        TextView attachGalleryPhoto = view.findViewById(R.id.attachGallery);
        TextView TimedMessage = view.findViewById(R.id.delayMessage);
        TextView videoMessage = view.findViewById(R.id.videoMessage);
        TextView contact = view.findViewById(R.id.contact);
        TextView document = view.findViewById(R.id.document);
        this.setCancelable(true);
        attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.attachFile);
            }
        });
        attachLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.location);
            }
        });
        attachCameraPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.camera);
            }
        });
        attachGalleryPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.gallery);
            }
        });
        TimedMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.delay);
            }
        });
        videoMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.video);
            }
        });
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.contact);
            }
        });
        document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBottomSheetClick(ButtonType.document);
            }
        });*/
        return view;
    }

}
