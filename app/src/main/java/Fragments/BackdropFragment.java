package Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.woofmeow.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import Consts.ConversationType;
import NormalObjects.Message;

@SuppressWarnings("Convert2Lambda")
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
    private onBottomSheetAction callback;

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
            Message message = (Message) bundle.getSerializable("message");
            int conversationType = bundle.getInt("conversationType",-1);
            TextView senderNameTv = coordinatorLayout.findViewById(R.id.messageSender);
            TextView messageTv = coordinatorLayout.findViewById(R.id.message);
            TextView messageTime = coordinatorLayout.findViewById(R.id.messageTime);
            TextView messageSeen = coordinatorLayout.findViewById(R.id.messageSeen);
            if (message.getSenderName() == null)
                senderNameTv.setText("name " + message.getGroupName());
            else
                senderNameTv.setText(message.getSenderName());
            //messageTv.setText(message.getMessage());
            messageTime.setText("sending time " + message.getSendingTime());
            messageSeen.setText("arrival time " + message.getArrivingTime());
            LinearLayout picking = coordinatorLayout.findViewById(R.id.picking);
            SwipeRefreshLayout refreshLayout = coordinatorLayout.findViewById(R.id.swipeRefresh);
            if (conversationType!=-1) {
                ConversationType type = ConversationType.values()[conversationType];
                switch (type) {
                    case single:
                        picking.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.conversation_cell_not_selected, requireContext().getTheme()));
                        refreshLayout.setBackgroundColor(ResourcesCompat.getColor(requireContext().getResources(), android.R.color.holo_blue_light,requireContext().getTheme()));
                        break;
                    case group:
                        picking.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.conversation_group_cell_not_selected, requireContext().getTheme()));
                        refreshLayout.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.conversation_group_cell_selected, requireContext().getTheme()));
                        break;
                    case sms:
                        picking.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.conversation_sms_cell_not_selected, requireContext().getTheme()));
                        refreshLayout.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.conversation_sms_cell_selected, requireContext().getTheme()));
                        break;
                    default:
                        Log.e("backdrop", "setting background doesn't work");
                }
            }
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
