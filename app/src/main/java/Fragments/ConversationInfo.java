package Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.woofmeow.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import NormalObjects.Conversation;

public class ConversationInfo extends BottomSheetDialogFragment {

    public static ConversationInfo getInstance(){
        Bundle args = new Bundle();
        ConversationInfo fragment = new ConversationInfo();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.conversation_info, container, false);
        Bundle bundle = getArguments();
        if(bundle!=null) {
            Conversation conversation = (Conversation) bundle.getSerializable("conversation");
            List<String>conversations = new ArrayList<>();
            conversations.add("conversationID: " + conversation.getConversationID());
            conversations.add("conversation name: " + conversation.getConversationName());
            conversations.add("conversation last message id: " + conversation.getLastMessageID());
            conversations.add("conversation last message: " + conversation.getLastMessage());
            conversations.add("conversation last message time: " + conversation.getLastMessageTime());
            conversations.add("conversation recipient name: " + conversation.getRecipientName());
            conversations.add("conversation sender name: " + conversation.getSenderName());
            ListView listView = linearLayout.findViewById(android.R.id.list);
            ArrayAdapter<String>adapter = new ArrayAdapter<>(requireContext(),R.layout.text_cell,R.id.infoText,conversations);
            listView.setAdapter(adapter);
        }
        return linearLayout;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return  dialog;
    }
    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet!=null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

            int windowHeight = getWindowHeight();
            if (layoutParams != null) {
                layoutParams.height = windowHeight / 2;
            }
            bottomSheet.setLayoutParams(layoutParams);
            behavior.setDraggable(false);
            bottomSheet.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    if (layoutParams != null)
                        layoutParams.height = windowHeight / 2;
                    return true;
                }
            });
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
