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

import Adapters.ListAdapter2;
import NormalObjects.Conversation;
import NormalObjects.onDismissFragment;

@SuppressWarnings("Convert2Lambda")
public class ConversationInfo extends BottomSheetDialogFragment {

    public static ConversationInfo getInstance(){
        Bundle args = new Bundle();
        ConversationInfo fragment = new ConversationInfo();
        fragment.setArguments(args);
        return fragment;
    }

    private onDismissFragment listener;

    public void setDismissListener(onDismissFragment listener)
    {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.conversation_info, container, false);
        Bundle bundle = getArguments();
        if(bundle!=null) {
            Conversation conversation = (Conversation) bundle.getSerializable("conversation");
            List<String>conversations = new ArrayList<>();
            conversations.add(conversation.getConversationID());
            conversations.add( conversation.getConversationName());
            conversations.add(conversation.getLastMessage());
            conversations.add(conversation.getLastMessageTimeParse());
            ListView listView = linearLayout.findViewById(android.R.id.list);
            ListAdapter2 listAdapter2 = new ListAdapter2();
            List<String>titles = new ArrayList<>();
            titles.add("Conversation ID");
            titles.add("Name");
            titles.add("Last Message");
            titles.add("Last Message Time");
            listAdapter2.setTitles(titles);
            listAdapter2.setItems(conversations);
            listAdapter2.setTextColor(getResources().getColor(android.R.color.white, requireActivity().getTheme()), getResources().getColor(R.color.black, requireActivity().getTheme()));
//            ArrayAdapter<String>adapter = new ArrayAdapter<>(requireContext(),R.layout.text_cell,R.id.infoText,conversations);
            listView.setAdapter(listAdapter2);
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

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener!=null)
            listener.onDismiss();
    }
}
