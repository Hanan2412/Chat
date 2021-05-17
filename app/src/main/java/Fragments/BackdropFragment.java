package Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.woofmeow.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BackdropFragment extends BottomSheetDialogFragment {

    public static BackdropFragment newInstance() {

        Bundle args = new Bundle();
        BackdropFragment fragment = new BackdropFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface onBottomSheetAction{
        void bottomSheetGone();
    }
    onBottomSheetAction callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (onBottomSheetAction) context;
        }catch (ClassCastException e)
        {
            throw new ClassCastException("Activity must implement onBottomSheetAction");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) inflater.inflate(R.layout.backdrop_layout, container, false);

        Bundle bundle = getArguments();
        if(bundle!=null) {
            String messageTxt = bundle.getString("message");
            String timeSent = bundle.getString("timeSent");
            String senderName = bundle.getString("senderName");
            String messageType = bundle.getInt("messageType") + "";
            boolean seen = bundle.getBoolean("messageSeen");
            String conversationID = bundle.getString("conversationID");
            TextView conversationIDTv = coordinatorLayout.findViewById(R.id.conversationID);
            String postID = "conversationID:" + conversationID;
            conversationIDTv.setText(postID);
            TextView senderNameTv = coordinatorLayout.findViewById(R.id.messageSender);
            String postName = "Sender Name: " + senderName;
            senderNameTv.setText(postName);
            TextView messageTv = coordinatorLayout.findViewById(R.id.message);
            String postMessage = "Message: " + messageTxt;
            messageTv.setText(postMessage);
            TextView messageTime = coordinatorLayout.findViewById(R.id.messageTime);
            String postTime = "Time sent: " + timeSent;
            messageTime.setText(postTime);
            TextView messageTypeTv = coordinatorLayout.findViewById(R.id.messageType);
            String postType = "Message Type: " + messageType;
            messageTypeTv.setText(postType);
            TextView messageSeen = coordinatorLayout.findViewById(R.id.messageSeen);
            String seenString  = "message seen: " + seen;
            messageSeen.setText(seenString);

            ImageView imageView = coordinatorLayout.findViewById(R.id.reportMessageButton);
            LinearLayout contentLayout = coordinatorLayout.findViewById(R.id.contentLayout);
            BottomSheetBehavior<View> sheetBehavior = BottomSheetBehavior.from(contentLayout);
            sheetBehavior.setFitToContents(false);
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            sheetBehavior.setDraggable(true);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFilters(sheetBehavior);
                }
            });

        }
        return coordinatorLayout;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        callback.bottomSheetGone();
    }

    private void toggleFilters(BottomSheetBehavior<View> sheetBehavior){

        if(sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            sheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        else
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}
