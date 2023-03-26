package Fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.ConversationActivity2;
import com.example.woofmeow.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import Adapters.ConversationsAdapter2;

import Backend.ConversationVM;
import Backend.UserVM;
import Consts.Tabs;
import NormalObjects.Conversation;
import NormalObjects.ConversationTouch;

import NormalObjects.TouchListener;
import NormalObjects.User;
import NormalObjects.onDismissFragment;
import Retrofit.Server;

import static android.content.Context.MODE_PRIVATE;

@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
public class TabFragment extends Fragment {

    private static final String tabNumber = "tabNumber";
    private String currentUser;
    private User user;
    private ConversationsAdapter2 conversationsAdapter2;
    private RecyclerView recyclerView;
    private final String FCM_ERROR = "fcm error";
    private LinearLayout searchLayout;
    private UserVM userModel;
    private ConversationVM conversationVM;
    private final String TAB_FRAGMENT = "TAB_FRAGMENT";
    private List<Conversation> selectedConversations;
    private final int online = 0, standby = 1, offline = 2;
    private int currentStatus = online;
    private EditText searchQuery;

    public static TabFragment newInstance(int tabNumber, String currentUser) {
        TabFragment tabFragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TabFragment.tabNumber, tabNumber);
        bundle.putString("currentUser", currentUser);
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    /**
     * an interface to update the main activity class
     */
    public interface UpdateMain {
        /**
         * sends the updated user
         *
         * @param user the user that is updated
         */
        void onUserUpdate(User user);

        /**
         * called when conversation is opened
         *
         * @param conversationID the conversation id of the conversation that was opened
         */
        void onOpenedConversation(String conversationID);
    }

    private UpdateMain callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (UpdateMain) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement UpdateMain interface");
        }
    }

    public TabFragment() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        assert getArguments() != null;
        currentUser = getArguments().getString("currentUser");
        setHasOptionsMenu(true);
        Tabs tab = Tabs.values()[getArguments().getInt(tabNumber)];
        if (selectedConversations == null)
            selectedConversations = new ArrayList<>();
        initGeneral();
        switch (tab) {
            case chat: {
                conversationsAdapter2 = new ConversationsAdapter2();
                initChatTab();
                view = inflater.inflate(R.layout.conversations_layout2, container, false);
                searchLayout = view.findViewById(R.id.searchLayout);
                searchQuery = view.findViewById(R.id.searchText);
                Button searchBtn = view.findViewById(R.id.searchBtn);
                ImageButton clearText = view.findViewById(R.id.clear_text);
                clearText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchQuery.setText("");
                        conversationsAdapter2.searchForConversations("");
                    }
                });
                searchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String search = searchQuery.getText().toString();
                        conversationsAdapter2.searchForConversations(search);
                    }
                });
                recyclerView = view.findViewById(R.id.recycle_view);
                recyclerView.setHasFixedSize(true);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setItemViewCacheSize(20);
                recyclerView.setDrawingCacheEnabled(true);
                recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                recyclerView.setAdapter(conversationsAdapter2);
                ConversationTouch touch = new ConversationTouch(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
                touch.setListener(new TouchListener() {
                    @Override
                    public void onSwipe(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        if (direction == ItemTouchHelper.LEFT) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setMessage(getResources().getString(R.string.delete_conversation_confirm_msg))
                                    .setTitle(getResources().getString(R.string.confirm))
                                    .setCancelable(true)
                                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_delete_24, requireActivity().getTheme()))
                                    .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Conversation conversation = conversationsAdapter2.getConversation(viewHolder.getAdapterPosition());
                                            deleteConversation(conversation.getConversationID());
                                            displayMessageToast(getResources().getString(R.string.select_conversation_delete));
                                        }
                                    }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //doesn't do anything since this button also dismisses the dialog - on dismiss runs and restores the conversation view
                                        }
                                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            conversationsAdapter2.notifyItemChanged(viewHolder.getAdapterPosition());
                                            displayMessageToast(getResources().getString(R.string.nothing_delete));
                                        }
                                    }).create().show();

                        }
                        //swiping right will promote to mute the conversation
                        else if (direction == ItemTouchHelper.RIGHT) {
                            muteConversation(conversationsAdapter2.getConversation(viewHolder.getAdapterPosition()));
                        }
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }
                });
                touch.setConversations(conversationsAdapter2);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touch);
                itemTouchHelper.attachToRecyclerView(recyclerView);
                recyclerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            Log.d(TAB_FRAGMENT, "click on recyclerview");
                            if (!selectedConversations.isEmpty())
                                unselectConversations();
                        }
                        return false;
                    }
                });
                conversationsAdapter2.setLongPressListener(new ConversationsAdapter2.onLongPress() {
                    @Override
                    public void onConversationLongPress(Conversation conversation) {
                        int x = selectedConversations.size();
                        selectedConversations.removeIf(selectedConversation -> selectedConversation.getConversationID().equals(conversation.getConversationID()));
                        int y = selectedConversations.size();
                        if (y >= x)
                            selectedConversations.add(conversation);
                        requireActivity().invalidateOptionsMenu();
                    }
                });
                conversationsAdapter2.setListener(new ConversationsAdapter2.onPressed() {
                    @Override
                    public void onClicked(Conversation conversation) {
                        unselectConversations();
                        Intent startConversationIntent = new Intent(requireActivity(), ConversationActivity2.class);
                        startConversationIntent.putExtra("conversationID", conversation.getConversationID());
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("share", MODE_PRIVATE);
                        String title = sharedPreferences.getString("title", "noTitle");
                        String link = sharedPreferences.getString("link", "noLink");
                        if (!link.equals("noLink")) {
                            startConversationIntent.putExtra("title", title);
                            startConversationIntent.putExtra("link", link);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("title");
                            editor.remove("link");
                            editor.apply();
                        }
                        callback.onOpenedConversation(conversation.getConversationID());
                        requireActivity().startActivity(startConversationIntent);

                    }
                });
                break;
            }
            case somethingElse:
                view = inflater.inflate(R.layout.coming_soon_layout, container, false);
        }
        return view;
    }

    private void displayMessageToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void unselectConversations() {
        for (int i = 0; i < selectedConversations.size(); i++) {
            Conversation conversation = selectedConversations.get(i);
            int selectedConversationIndex = conversationsAdapter2.findCorrectConversationIndex(conversation.getConversationID());
            if (recyclerView != null) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(selectedConversationIndex);
                if (holder != null) {
                    i--;
                    holder.itemView.performLongClick();
                }
            }
        }
        selectedConversations.clear();
        requireActivity().invalidateOptionsMenu();
    }

    private void pinConversations() {
        for (Conversation conversation : selectedConversations) {
            conversationsAdapter2.pinConversation(conversation, true);
            conversation.setPinned(true);
            updateConversation(conversation);
        }
        restoreBaseMenu();
    }

    private void unPinConversation() {
        for (Conversation conversation : selectedConversations) {
            conversationsAdapter2.pinConversation(conversation, false);
            updateConversation(conversation);
        }
        restoreBaseMenu();
    }

    private void restoreBaseMenu() {
        unselectConversations();
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.pinConversation) {
            pinConversations();
        } else if (item.getItemId() == R.id.unpin) {
            unPinConversation();
        } else if (item.getItemId() == R.id.info) {
            ConversationInfo conversationInfo = ConversationInfo.getInstance();
            Bundle backDropBundle = new Bundle();
            backDropBundle.putSerializable("conversation", selectedConversations.get(0));
            conversationInfo.setArguments(backDropBundle);
            conversationInfo.setDismissListener(new onDismissFragment() {
                @Override
                public void onDismiss() {
                    unselectConversations();
                    requireActivity().invalidateOptionsMenu();
                }
            });
            conversationInfo.show(requireActivity().getSupportFragmentManager(), "CONVERSATION_INFO");
        } else if (item.getItemId() == R.id.callBtn) {
            callPhone();
        } else if (item.getItemId() == R.id.block) {
            blockConversation();
            unselectConversations();
        } else if (item.getItemId() == R.id.searchConversation) {
            Animation in = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down);
            Animation out = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up);
            if (searchLayout.getVisibility() == View.GONE) {
                searchLayout.setVisibility(View.VISIBLE);
                searchLayout.startAnimation(in);
                recyclerView.startAnimation(in);
                if (searchQuery != null)
                    searchQuery.setText("");

            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchLayout.setVisibility(View.GONE);
                    }
                }, out.getDuration());
                searchLayout.startAnimation(out);
                recyclerView.startAnimation(out);
                loadConversations();
            }
        } else if (item.getItemId() == R.id.status) {
            user.setStatus((user.getStatus() + 1) % 3);
            updateUser(user);
        } else if (item.getItemId() == R.id.addPhoneNumber) {
            SingleFieldFragment phoneFragment = new SingleFieldFragment();
            phoneFragment.setHint(getResources().getString(R.string.phone_number));
            phoneFragment.setInputType(InputType.TYPE_CLASS_PHONE);
            phoneFragment.setListener(new SingleFieldFragment.onText() {
                @Override
                public void onTextChange(String name) {
                    selectedConversations.get(0).setRecipientPhoneNumber(name);
                    updateConversation(selectedConversations.get(0));
                }
            });
            phoneFragment.setOnDismissListener(new SingleFieldFragment.onDismiss() {
                @Override
                public void onDismissFragment() {
                    unselectConversations();
                }
            });
            phoneFragment.show(requireActivity().getSupportFragmentManager(), "PhoneFragment");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.setGroupVisible(R.id.normal, selectedConversations.isEmpty());
        menu.setGroupVisible(R.id.extraOptions, !selectedConversations.isEmpty());
        if (user != null) {
            onChangeStatus(menu.findItem(R.id.status), user.getStatus());
        }
        if (selectedConversations.size() > 1) {
            menu.setGroupVisible(R.id.pin, false);
            menu.setGroupVisible(R.id.singleConversationOptions, false);
        } else if (selectedConversations.size() == 1) {

            menu.setGroupVisible(R.id.pin, true);
            if (conversationsAdapter2.getConversation(0).isPinned() && !selectedConversations.get(0).isPinned()) {
                menu.findItem(R.id.pinConversation).setVisible(false);
                menu.findItem(R.id.unpin).setVisible(false);
            } else {
                menu.findItem(R.id.pinConversation).setVisible(!selectedConversations.get(0).isPinned());
                menu.findItem(R.id.unpin).setVisible(selectedConversations.get(0).isPinned());
            }
            menu.setGroupVisible(R.id.singleConversationOptions, true);
            if (selectedConversations.get(0).getRecipientPhoneNumber() == null)
                menu.findItem(R.id.callBtn).setVisible(false);
            if (selectedConversations.get(0).isBlocked())
                menu.findItem(R.id.block).setTitle("unblock");
            if (selectedConversations.get(0).getRecipientPhoneNumber() == null)
                menu.setGroupVisible(R.id.singleConversationExtraOptions, true);
        }
    }

    private void dataBaseSetUp() {
        userModel = new ViewModelProvider(this).get(UserVM.class);
        conversationVM = new ViewModelProvider(this).get(ConversationVM.class);
    }

    private void onUserUpdate() {
        userModel.getCurrentUser().observe(requireActivity(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                Log.d(TAB_FRAGMENT, "loaded current user: " + user);
                if (user != null) {
                    setCurrentUser(user);
                } else {
                    Log.d(TAB_FRAGMENT, "loaded null user");
                }
            }
        });
    }

    private void onDownloadUser() {
        userModel.setOnUserDownloadedListener(new Server.onUserDownload() {
            @Override
            public void downloadedUser(User user) {
                if (user != null) {
                    setCurrentUser(user);
                    updateUser(user);
                }
            }
        });
        userModel.downloadUser(currentUser);
    }

    private void setCurrentUser(User user) {
        if (this.user != null) {
            if (user.getLastUpdateTime() > this.user.getLastUpdateTime()) {
                Log.d(TAB_FRAGMENT, "setting updated user");
                this.user = user;
            } else {
                Log.e(TAB_FRAGMENT, "loaded older version of user, ignoring it");
            }
        } else {
            this.user = user;
        }
        callback.onUserUpdate(user);
        requireActivity().invalidateOptionsMenu();
    }

    private void tokenUpdate() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful())
                    Log.e(FCM_ERROR, "Fetching FCM registration token failed: " + task.getException());
                else {
                    String token = task.getResult();
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Token", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("token", token);
                    editor.apply();
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        userModel.updateToken(currentUserUID, token);
                        Log.d("token", "updated token");
                    }
                }
            }
        });
    }

    //calls all the functions needed to start the fragment
    private void initChatTab() {
        NullifyData();
        loadConversations();
        onNewOrUpdatedConversation();
    }

    private void initGeneral() {
        dataBaseSetUp();
        onUserUpdate();
        onDownloadUser();
        tokenUpdate();
    }

    private void NullifyData() {
        SharedPreferences conversationPreferences = requireActivity().getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", "no conversation");
        editor.apply();
    }


    //called when the fragment is lunched
    private void loadConversations() {
        LiveData<List<Conversation>> conversationLiveData = conversationVM.getConversations();
        conversationLiveData.observe(requireActivity(), new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                conversationsAdapter2.setConversations(conversations);
                conversationLiveData.removeObserver(this);
            }
        });
    }

    private void onNewOrUpdatedConversation() {
        conversationVM.getNewOrUpdatedConversation().observe(requireActivity(), new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                if (conversation != null) {
                    conversationsAdapter2.updateConversation(conversation);
                }
            }
        });
    }

    private void muteConversation(Conversation conversation) {
        conversation.setMuted(!conversation.isMuted());
        updateConversation(conversation);
        conversationsAdapter2.muteConversation(conversation);
    }

    private void blockConversation() {
        for (Conversation conversation : selectedConversations) {
            conversation.setBlocked(!conversation.isBlocked());
            updateConversation(conversation);
            conversationsAdapter2.blockConversation(conversation);
        }
    }

    private void callPhone() {
        Log.d(TAB_FRAGMENT, "callPhone");
        String phoneNumber = selectedConversations.get(0).getRecipientPhoneNumber();
        Intent callRecipientIntent = new Intent(Intent.ACTION_DIAL);
        callRecipientIntent.setData(Uri.parse("tel:" + phoneNumber));
        unselectConversations();
        requireActivity().invalidateOptionsMenu();
        if (callRecipientIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            Log.d(TAB_FRAGMENT, "starting dial");
            startActivity(callRecipientIntent);
        } else {
            Log.e(TAB_FRAGMENT, "can't start dial");
        }
    }

    private void deleteConversation(String conversationID) {
        Log.d(TAB_FRAGMENT, "delete conversation: " + conversationID);
        conversationsAdapter2.deleteConversation(conversationID);
        conversationVM.deleteConversation(conversationID);
    }

    private void updateConversation(Conversation conversation) {
        Log.d(TAB_FRAGMENT, "update conversation: " + conversation.getConversationID());
        conversationVM.updateConversation(conversation);
    }

    private void onChangeStatus(MenuItem item, int newStatus) {
        switch (newStatus) {
            case online:
                item.setIcon(R.drawable.circle_green);
                currentStatus = online;
                break;
            case offline:
                item.setIcon(R.drawable.circle_red);
                currentStatus = offline;
                break;
            case standby:
                item.setIcon(R.drawable.circle_yellow);
                currentStatus = standby;
                break;
        }
        Log.d(TAB_FRAGMENT, "changed status: " + currentStatus);
    }

    public void updateUser(User user) {
        Log.d(TAB_FRAGMENT, "update user: " + user.getUserUID());
        user.setLastUpdateTime(System.currentTimeMillis());
        userModel.updateUser(user);
    }
}
