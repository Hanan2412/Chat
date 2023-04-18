package Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.woofmeow.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;


import Backend.ConversationVM;
import NormalObjects.Message;
import NormalObjects.MessageHistory;


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
            BottomNavigationView bottomNavigationView = coordinatorLayout.findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.messageHistory) {
                        ConversationVM model = new ViewModelProvider(requireActivity()).get(ConversationVM.class);
                        model.getMessageHistories(message.getMessageID()).observe(requireActivity(), new Observer<List<MessageHistory>>() {
                            @Override
                            public void onChanged(List<MessageHistory> messageHistories) {
                                model.getMessageHistories(message.getMessageID()).removeObserver(this);
//                                MessageHistoryFragment messageHistoryFragment = new MessageHistoryFragment(messageHistories);
//                                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.root_container, messageHistoryFragment).commit();
                            }
                        });


                    }
                    else if (item.getItemId() == R.id.messageInfo)
                    {
//                        MessageInfoFragment infoFragment = new MessageInfoFragment(message);
//                        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.root_container, infoFragment).commit();

                    }
                    return false;
                }
            });
            bottomNavigationView.setSelectedItemId(R.id.messageHistory);


            RelativeLayout contentLayout = coordinatorLayout.findViewById(R.id.contentLayout);
            BottomSheetBehavior<View> sheetBehavior = BottomSheetBehavior.from(contentLayout);
            sheetBehavior.setFitToContents(false);
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            sheetBehavior.setDraggable(false);

//            ImageView imageView = coordinatorLayout.findViewById(R.id.reportMessageButton);
//            imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    toggleFilters(sheetBehavior);
//                }
//            });

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
