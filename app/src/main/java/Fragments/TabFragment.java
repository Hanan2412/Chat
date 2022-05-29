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

import android.util.Log;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import Adapters.ConversationsAdapter2;

import Backend.ConversationVM;
import Backend.UserVM;
import Consts.Tabs;
import NormalObjects.Conversation;
import NormalObjects.ConversationTouch;

import NormalObjects.TouchListener;
import NormalObjects.User;
import Retrofit.Server;

import static android.content.Context.MODE_PRIVATE;
import static com.example.woofmeow.MainActivity.OFFLINE_S;
import static com.example.woofmeow.MainActivity.ONLINE_S;
import static com.example.woofmeow.MainActivity.STANDBY_S;


@SuppressWarnings("Convert2Lambda")
public class TabFragment extends Fragment {

    private static final String tabNumber = "tabNumber";
    private String currentUser;
    private User user;
    private String currentStatus = ONLINE_S;
    private boolean openingActivity = false;
    private ConversationsAdapter2 conversationsAdapter2;
    private boolean selected = false;
    private RecyclerView recyclerView;
    private final String FCM_ERROR = "fcm error";
    private LinearLayout searchLayout;
    private UserVM userModel;
    private ConversationVM conversationVM;
    private final String pin = "pin";

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
         * loads user from memory
         *
         * @param user the user to load from memory
         */
        void onLoadUserFromMemory(User user);

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
        switch (tab) {
            case chat: {
                conversationsAdapter2 = new ConversationsAdapter2();
                init();
                view = inflater.inflate(R.layout.conversations_layout2, container, false);
                searchLayout = view.findViewById(R.id.searchLayout);
                Button searchBtn = view.findViewById(R.id.searchBtn);
                EditText searchQuery = view.findViewById(R.id.searchText);
                searchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String search = searchQuery.getText().toString();
                        if (search != null)
                            conversationsAdapter2.Search(search);
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
                            builder.setMessage("Are you sure you would like to delete the selected conversations? this action can't be undone")
                                    .setTitle("Confirm Action")
                                    .setCancelable(true)
                                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_delete_24, requireActivity().getTheme()))
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Conversation conversation = conversationsAdapter2.getConversation(viewHolder.getAdapterPosition());
                                            deleteConversation(conversation.getConversationID());
                                            Toast.makeText(requireContext(), "Selected conversations were deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    conversationsAdapter2.notifyItemChanged(viewHolder.getAdapterPosition());
                                    Toast.makeText(requireContext(), "Nothing will be deleted", Toast.LENGTH_SHORT).show();
                                }
                            }).create().show();

                        }
                        //swiping right will promote to mute the conversation
                        else if (direction == ItemTouchHelper.RIGHT) {
                            muteConversation(conversationsAdapter2.getConversation(viewHolder.getAdapterPosition()).getConversationID());
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
                conversationsAdapter2.setLongPressListener(new ConversationsAdapter2.onLongPress() {
                    @Override
                    public void onConversationLongPress(Conversation conversation) {
                        selected = true;
                        requireActivity().invalidateOptionsMenu();
                    }
                });

                conversationsAdapter2.setListener(new ConversationsAdapter2.onPressed() {

                    @Override
                    public void onClicked(Conversation conversation) {
                        if (selected)
                        {
                            unSelectAll();
                            selected = false;
                        }else {
                            Intent startConversationIntent = new Intent(requireActivity(), ConversationActivity.class);
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
                    }

                    @Override
                    public void onImageDownloaded(Conversation conversation, boolean image) {
                        UpdateConversationsInDataBase(conversation, image);
                    }
                });
                break;
            }
            case somethingElse:
                view = inflater.inflate(R.layout.coming_soon_layout, container, false);
        }
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.pinConversation)
            pinConversation();
        else if (item.getItemId() == R.id.unpin)
            unPinConversation();
        else if (item.getItemId() == R.id.callBtn) {
            if (conversationsAdapter2.getSelectedConversations().size() == 1)
                callPhone();
        } else if (item.getItemId() == R.id.block)
            blockConversation();
        else if (item.getItemId() == R.id.searchConversation) {
            Animation in = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down);
            Animation out = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up);
            if (searchLayout.getVisibility() == View.GONE) {
                searchLayout.setVisibility(View.VISIBLE);
                searchLayout.startAnimation(in);
                recyclerView.startAnimation(in);

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
            switch (currentStatus) {
                case ONLINE_S:
                    item.setIcon(R.drawable.circle_red);
                    currentStatus = OFFLINE_S;
                    ChangeStatus(OFFLINE_S);
                    break;
                case OFFLINE_S:
                    item.setIcon(R.drawable.circle_yellow);
                    currentStatus = STANDBY_S;
                    ChangeStatus(STANDBY_S);
                    break;
                case STANDBY_S:
                    item.setIcon(R.drawable.circle_green);
                    currentStatus = ONLINE_S;
                    ChangeStatus(ONLINE_S);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void ChangeStatus(String currentStatus) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Status", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean status = preferences.getBoolean("status", true);//settings preference - allow contacts to see your online status
        if (user != null) {
            if (status) {
                user.setStatus(currentStatus);
                editor.putString("status", currentStatus);
            } else {
                user.setStatus(OFFLINE_S);
                editor.putString("status", OFFLINE_S);
            }
            userModel.updateUser(user);
        }
        editor.apply();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(conversationsAdapter2!=null) {
            if (conversationsAdapter2.getSelectedConversations().isEmpty()) {
                menu.setGroupVisible(R.id.active, false);
                menu.setGroupVisible(R.id.standBy, true);
            } else {
                menu.setGroupVisible(R.id.active, true);
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("conversations", MODE_PRIVATE);
                if (sharedPreferences.contains(pin)) {
                    String conversationID = sharedPreferences.getString(pin, "");
                    menu.findItem(R.id.unpin).setVisible(conversationsAdapter2.getSelectedConversations().get(0).getConversationID().equals(conversationID));
                    menu.findItem(R.id.pinConversation).setVisible(!conversationsAdapter2.getSelectedConversations().get(0).getConversationID().equals(conversationID));
                } else menu.findItem(R.id.unpin).setVisible(false);
                menu.setGroupVisible(R.id.standBy, false);
            }
        }
    }

    private void callPhone() {
        //needs to check for recipient phone number, if it doesn't exist - display appropriate message
        if (conversationsAdapter2.getSelectedConversations().size() == 1) {
            String phoneNumber = conversationsAdapter2.getSelectedConversations().get(0).getRecipientPhoneNumber();
            if (phoneNumber != null) {
                Intent callRecipientIntent = new Intent(Intent.ACTION_DIAL);
                callRecipientIntent.setData(Uri.parse("tel:" + phoneNumber));
                if (callRecipientIntent.resolveActivity(requireActivity().getPackageManager()) != null)
                    startActivity(callRecipientIntent);
            } else
                Toast.makeText(requireContext(), "this contact has no phone number", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(requireContext(), "can only call to one person at a time", Toast.LENGTH_SHORT).show();
        requireActivity().invalidateOptionsMenu();
        unSelectAll();
    }

    private void unSelectAll() {
        List<Conversation> selectedConversations = conversationsAdapter2.getSelectedConversations();
        for (int i = 0;i<selectedConversations.size();i++) {
            Conversation conversation = selectedConversations.get(i);
            int selectedConversationIndex = conversationsAdapter2.findCorrectConversationIndex(conversation.getConversationID());
            if (recyclerView != null) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(selectedConversationIndex);
                if (holder != null)
                {
                    i--;
                    holder.itemView.performLongClick();
                }
            }
        }
    }

    private void blockConversation() {
        if (!conversationsAdapter2.getSelectedConversations().isEmpty()) {
            String conversationID = conversationsAdapter2.getSelectedConversations().get(0).getConversationID();
            LiveData<Boolean> blockedConversation = conversationVM.isConversationBlocked(conversationID);
            blockedConversation.observe(requireActivity(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    if (aBoolean) {
                        builder.setTitle("un block conversation")
                                .setMessage("unblock this conversation to start receiving messages from the conversation")
                                .setPositiveButton("unblock", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        conversationVM.unBlockConversation(conversationID);
                                        onConversationStatusUpdate(conversationID);
                                        Toast.makeText(requireContext(), "conversation was unblocked", Toast.LENGTH_SHORT).show();
                                    }
                                }).setCancelable(true)
                                .create()
                                .show();
                    } else {
                        builder.setTitle("block conversation")
                                .setMessage("block this conversation to stop receiving messages from this conversation")
                                .setPositiveButton("block", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        conversationVM.blockConversation(conversationID);
                                        onConversationStatusUpdate(conversationID);
                                        Toast.makeText(requireContext(), "conversation was blocked", Toast.LENGTH_SHORT).show();
                                    }
                                }).setCancelable(true)
                                .create()
                                .show();

                    }
                    blockedConversation.removeObservers(requireActivity());
                }
            });
            requireActivity().invalidateOptionsMenu();
            unSelectAll();

        }
    }

    private void pinConversation() {
        if (conversationsAdapter2.getSelectedConversations().size() == 1) {
            SharedPreferences sp = requireContext().getSharedPreferences("conversations", MODE_PRIVATE);
            String oldPin = sp.getString(pin, "");
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(pin, conversationsAdapter2.getSelectedConversations().get(0).getConversationID());
            editor.apply();
            conversationsAdapter2.pinConversation(conversationsAdapter2.getSelectedConversations().get(0), oldPin);

        } else {
            Toast.makeText(requireContext(), "can't pin more than 1 conversation", Toast.LENGTH_SHORT).show();
        }

        requireActivity().invalidateOptionsMenu();
        unSelectAll();
    }

    private void unPinConversation() {
        SharedPreferences sp = requireContext().getSharedPreferences("conversations", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("pin");
        editor.apply();
        conversationsAdapter2.unPinConversation();
        requireActivity().invalidateOptionsMenu();
        unSelectAll();
    }

    private void DataBaseSetUp() {
        userModel = new ViewModelProvider(this).get(UserVM.class);
        conversationVM = new ViewModelProvider(this).get(ConversationVM.class);
    }

    //if the user exists in the database - load it, if not - download it from firebase database

    /**
     * loads the current logged in user from the database.
     * if they doesn't exist, the function will download them from the server
     */
    private void LoadCurrentUser() {
        userModel.getCurrentUser().observe(requireActivity(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                TabFragment.this.user = user;
                if (user != null) {
                    callback.onLoadUserFromMemory(user);
                } else {
                    userModel.setOnUserDownloadedListener(new Server.onUserDownload() {
                        @Override
                        public void downloadedUser(User user) {
                            if (user.getUserUID().equals(currentUser)) {
                                userModel.insertUser(user);
                                callback.onUserUpdate(user);
                                TabFragment.this.user = user;
                            }
                        }
                    });
                    userModel.downloadUser(currentUser);
                }
            }
        });
    }


    private void UpdateConversationsInDataBase(Conversation conversation, boolean image) {
        conversationVM.updateConversation(conversation);
        //dbActive.updateConversation(conversation);
        if (!image)
            conversationsAdapter2.updateConversation(conversation);

    }

    private void TokenUpdate() {
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


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!openingActivity)
            ChangeStatus(OFFLINE_S);
        openingActivity = false;

    }

    @Override
    public void onResume() {
        super.onResume();
        ChangeStatus(ONLINE_S);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy fragment");
    }

    //calls all the functions needed to start the fragment
    private void init() {
        DataBaseSetUp();
        NullifyData();
        LoadCurrentUser();
        loadConversations();
        loadNewOrUpdatedConversation();
        TokenUpdate();
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
                conversationsAdapter2.setConversations((ArrayList<Conversation>) conversations);
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            try {
                                wait(250);
                                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("conversations", MODE_PRIVATE);
                                if (sharedPreferences.contains(pin)) {
                                    String conversationID = sharedPreferences.getString(pin, "");
                                    Handler handler = new Handler(requireContext().getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            conversationsAdapter2.pinConversation(conversationsAdapter2.findConversation(conversationID), conversationID);
                                        }
                                    });

                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                thread.start();
                conversationLiveData.removeObserver(this);
            }
        });
    }

    private void loadNewOrUpdatedConversation() {
        conversationVM.getNewOrUpdatedConversation().observe(requireActivity(), new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                if (conversation != null) {
                    Conversation existingConversation = conversationsAdapter2.findConversation(conversation.getConversationID());
                    if (existingConversation != null) {
                        conversationsAdapter2.updateConversation(conversation);
                    } else {
                        conversationsAdapter2.setConversation(conversation, 0);
                    }
                }
            }
        });
    }

    private void muteConversation(String conversationID) {

        LiveData<Boolean> mutedConversation = conversationVM.isConversationMuted(conversationID);
        mutedConversation.observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                String dialog;
                if (!aBoolean) {
                    dialog = "Conversation was Muted";
                    conversationVM.muteConversation(conversationID);

                } else {
                    dialog = "Conversation was unMuted";
                    conversationVM.unMuteConversation(conversationID);
                }
                onConversationStatusUpdate(conversationID);
                mutedConversation.removeObservers(requireActivity());
                Snackbar.make(requireContext(), recyclerView, dialog, Snackbar.LENGTH_SHORT)
                        .setAction("undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                muteConversation(conversationID);
                            }
                        }).show();
                mutedConversation.removeObserver(this);
            }
        });

    }

    private void onConversationStatusUpdate(String conversationID) {
        LiveData<Conversation> liveData = conversationVM.loadConversation(conversationID);
        liveData.observe(requireActivity(), new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                conversationsAdapter2.updateConversation(conversation);
                liveData.removeObserver(this);
            }
        });
    }

    private void deleteConversation(String conversationID) {
        conversationsAdapter2.DeleteConversation(conversationID);
        conversationVM.deleteConversation(conversationID);
    }
}
