package Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.woofmeow.MainActivity;
import com.example.woofmeow.ProfileActivity3;
import com.example.woofmeow.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Adapters.ListAdapter2;
import Backend.ConversationVM;
import Backend.UserVM;
import NormalObjects.Message;
import NormalObjects.MessageHistory;
import NormalObjects.MessageViews;
import NormalObjects.User;
import NormalObjects.onDismissFragment;
import Time.TimeFormat;

@SuppressWarnings("Convert2Lambda")
public class MessageInfoFragment extends BottomSheetDialogFragment {

    private final String MESSAGE_FRAGMENT = "MESSAGE_FRAGMENT";
    private ListAdapter2 listAdapter;
    private final String MESSAGE = "Message";
    private final String SENDING_TIME = "Sending Time";
    private final String ARRIVING_TIME = "Arriving Time";
    private final String READING_TIME = "Reading Time";
    private final String SENDER = "Sender";
    private final String CONVERSATION_NAME = "Conversation Name";
    private final String SENT_TO = "Sent to";
    private final String DELIVERED_TIME = "Delivered Time";
    private TimeFormat timeFormat;
    private ConversationVM model;
    private UserVM userModel;

    public static MessageInfoFragment getInstance()
    {
        Bundle bundle = new Bundle();
        MessageInfoFragment msgFragment = new MessageInfoFragment();
        msgFragment.setArguments(bundle);
        return msgFragment;
    }

    private MessageInfoFragment()
    {

    }

    private onDismissFragment listener;

    public void setDismissListener(onDismissFragment listener)
    {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment_layout, container, false);
        model = new ViewModelProvider(this).get(ConversationVM.class);
        userModel = new ViewModelProvider(this).get(UserVM.class);
        Bundle bundle = getArguments();
        if (bundle!=null)
        {
            TextView title = view.findViewById(R.id.title);
            timeFormat = new TimeFormat();
            listAdapter = new ListAdapter2();
            List<String> titles = new ArrayList<>();
            List<String>content = new ArrayList<>();
            String currentUID = bundle.getString("currentUID");
            Message message = (Message) bundle.getSerializable("message");
            BottomNavigationView bottomNavigationView = view.findViewById(R.id.messageBottomNavigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.messageHistory)
                    {
                        Log.d(MESSAGE_FRAGMENT,"showing message history");
                        String title1 = getResources().getString(R.string.changes_message) + " " + message.getContent();
                        title.setText(title1);
                        titles.clear();
                        content.clear();
                        LiveData<List<MessageHistory>> messageHistoriesLV = model.getMessageHistories(message.getMessageID());
                        messageHistoriesLV.observe(requireActivity(), new Observer<List<MessageHistory>>() {
                            @Override
                            public void onChanged(List<MessageHistory> messageHistories) {
                                messageHistoriesLV.removeObservers(requireActivity());
                                for (int i = 0; i < messageHistories.size(); i++)
                                {
                                    titles.add("previous message " + (i+1));
                                    content.add(messageHistories.get(i).getContent());
                                }
                                listAdapter.setTitles(titles);
                                listAdapter.setItems(content);
                                ListFragment listFragment = new ListFragment();
                                listFragment.setAdapter(listAdapter);
                                listAdapter.notifyDataSetChanged();
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        getChildFragmentManager().beginTransaction().replace(R.id.placeholder, listFragment).commit();
                                    }
                                });

                            }
                        });


                    }
                    else if (item.getItemId() == R.id.info)
                    {
                        Log.d(MESSAGE_FRAGMENT,"showing message info");
                        title.setText(getResources().getString(R.string.info));
                        titles.clear();
                        content.clear();
                        titles.add(CONVERSATION_NAME);
                        content.add(message.getConversationName());
                        titles.add(MESSAGE);
                        content.add(message.getContent());
                        titles.add(SENDER);
                        content.add(message.getSenderName());
                        titles.add(SENDING_TIME);
                        content.add(timeFormat.getFormattedDate(message.getSendingTime()));
                        if (!message.getSenderID().equals(currentUID))
                        {
                            titles.add(ARRIVING_TIME);
                            content.add(timeFormat.getFormattedDate(message.getArrivingTime()));
                        }
                        else
                        {
                            titles.add(READING_TIME + " By You");
                            content.add(timeFormat.getFormattedDate(message.getReadingTime()));
                        }
                        listAdapter.setTitles(titles);
                        listAdapter.setItems(content);
                        ListFragment listFragment = new ListFragment();
                        listFragment.setAdapter(listAdapter);
                        listAdapter.notifyDataSetChanged();
                        getChildFragmentManager().beginTransaction().replace(R.id.placeholder, listFragment).commit();

                    }
                    else if (item.getItemId() == R.id.views)
                    {
                        Log.d(MESSAGE_FRAGMENT,"showing message views");
                        title.setText(getResources().getString(R.string.views_message));
                        titles.clear();
                        content.clear();
                        ListFragment listFragment = new ListFragment();
                        model.getMessageViews(message.getMessageID()).observe(requireActivity(), new Observer<List<MessageViews>>() {
                            @Override
                            public void onChanged(List<MessageViews> messageViews) {
                                for (int i = 0; i < messageViews.size(); i++)
                                {
                                    MessageViews messageView = messageViews.get(i);
                                    titles.add(SENT_TO);
                                    content.add(messageView.getUserName());
                                    titles.add(DELIVERED_TIME);
                                    if (messageView.getDeliveredTime() == 0)
                                        content.add("message wasn't delivered");
                                    else
                                        content.add(timeFormat.getFormattedDate(messageView.getDeliveredTime()));
                                    titles.add(READING_TIME);
                                    if (messageView.getReadTime() == 0)
                                        content.add("message wasn't read");
                                    else
                                        content.add(timeFormat.getFormattedDate(messageView.getReadTime()));
                                }
                                listAdapter.setTitles(titles);
                                listAdapter.setItems(content);
                                listFragment.setAdapter(listAdapter);
                                listAdapter.notifyDataSetChanged();
                                listFragment.setListener(new ListFragment.ItemClickListener() {
                                    @Override
                                    public void onClickItem(int position) {
                                        if (position % 3 == 0)
                                        {
                                            int x = position / 3;
                                            String uid = messageViews.get(x).getUid();
                                            LiveData<User>userLV = userModel.loadUserByID(uid);
                                            userLV.observe(requireActivity(), new Observer<User>() {
                                                @Override
                                                public void onChanged(User user) {
                                                    userLV.removeObserver(this);
                                                    Intent intent = new Intent(requireActivity(), ProfileActivity3.class);
                                                    intent.putExtra("user", user);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    }
                                });
                                getChildFragmentManager().beginTransaction().replace(R.id.placeholder, listFragment).commit();
                            }
                        });

                    }
                    return true;
                }
            });
            bottomNavigationView.setSelectedItemId(R.id.info);
        }
        else
            Log.e(MESSAGE_FRAGMENT, "bundle is null");
        return view;
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
