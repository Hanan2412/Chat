package Fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

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
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.MainGUI;
import com.example.woofmeow.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import Adapters.ConversationsAdapter2;
import Consts.MessageAction;
import Consts.Tabs;
import Controller.CController;

import Model.MessageSender;
import NormalObjects.Conversation;
import NormalObjects.ConversationTouch;
import NormalObjects.Message;
import NormalObjects.TouchListener;
import NormalObjects.User;
import DataBase.*;
import static android.content.Context.MODE_PRIVATE;
import static com.example.woofmeow.MainActivity.OFFLINE_S;
import static com.example.woofmeow.MainActivity.ONLINE_S;
import static com.example.woofmeow.MainActivity.STANDBY_S;


@SuppressWarnings("Convert2Lambda")
public class TabFragment extends Fragment implements MainGUI {

    private static final String tabNumber = "tabNumber";
    private String currentUser;
    private ArrayList<Conversation> conversations = new ArrayList<>();
    private CController controller;
    private User user;
    private String currentStatus = ONLINE_S;
    private boolean openingActivity = false;
    private ConversationsAdapter2 conversationsAdapter2;
    private int selected = 0;
    private ArrayList<Conversation> selectedConversations = new ArrayList<>();
    private RecyclerView recyclerView;
    private final String FCM_ERROR = "fcm error";
    private View view;
    private LinearLayout searchLayout;
    private DBActive dbActive;

    public static TabFragment newInstance(int tabNumber, String currentUser) {
        TabFragment tabFragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TabFragment.tabNumber, tabNumber);
        bundle.putString("currentUser", currentUser);
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    public interface UpdateMain {
        void onUserUpdate(User user);
        void onLoadUserFromMemory(User user);
        void onNewMessage(String conversationID);
        void onOpenedConversation(String conversationID);
    }


    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        if (args != null && args.containsKey("query")) {
            controller.onFindUsersQuery((String) args.get("query"), requireContext());
        }
    }

    private UpdateMain callback;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        controller = CController.getController();
        controller.setMainGUI(this);
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
        view = null;
        assert getArguments() != null;
        currentUser = getArguments().getString("currentUser");
        setHasOptionsMenu(true);
        conversationsAdapter2 = new ConversationsAdapter2();
        init();
        Tabs tab = Tabs.values()[getArguments().getInt(tabNumber)];
        switch (tab) {
            case chat: {
                view = inflater.inflate(R.layout.conversations_layout2, container, false);
                searchLayout = view.findViewById(R.id.searchLayout);
                Button searchBtn = view.findViewById(R.id.searchBtn);
                EditText searchQuery = view.findViewById(R.id.searchText);
                searchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String search = searchQuery.getText().toString();
                        if (search!=null)
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
                    public void onSwipe(@NonNull  RecyclerView.ViewHolder viewHolder, int direction) {
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
                                            DeleteConversation(conversation.getConversationID());
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
                            MuteConversation(conversationsAdapter2.getConversation(viewHolder.getAdapterPosition()).getConversationID());
                        }
                    }

                    @Override
                    public boolean onMove(@NonNull  RecyclerView recyclerView, @NonNull  RecyclerView.ViewHolder viewHolder, @NonNull  RecyclerView.ViewHolder target) {
                        return false;
                    }
                });
                touch.setConversations(conversationsAdapter2);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touch);
                itemTouchHelper.attachToRecyclerView(recyclerView);

                conversationsAdapter2.setListener(new ConversationsAdapter2.onPressed() {
                    @Override
                    public void onLongPressed(boolean selected, Conversation conversation) {
                        if (selected) {
                            TabFragment.this.selected++;
                            selectedConversations.add(conversation);
                        } else {
                            selectedConversations.remove(conversation);
                            TabFragment.this.selected--;
                        }
                        requireActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onClicked(Conversation conversation) {
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

                    @Override
                    public void onImageDownloaded(Conversation conversation, boolean image) {
                        UpdateConversationsInDataBase(conversation, image);
                    }
                });
                LinearLayout rootLayout = view.findViewById(R.id.rootLayout);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                boolean darkMode = preferences.getBoolean("darkView", false);
                if (darkMode)
                    rootLayout.setBackgroundColor(getResources().getColor(android.R.color.black, requireActivity().getTheme()));
                else
                    rootLayout.setBackgroundColor(getResources().getColor(android.R.color.white, requireActivity().getTheme()));

                recyclerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (selected > 0) {
                            UnSelectAll();
                            return true;
                        } else return false;
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
            PinConversation();
        else if (item.getItemId() == R.id.callBtn) {
            if (selected == 1)
                CallPhone();
        } else if (item.getItemId() == R.id.block)
            BlockUser();
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
                LoadConversations();
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
        if(user!=null)
            if(status)
            {
                user.setStatus(currentStatus);
                editor.putString("status",currentStatus);
            }
            else
            {
                user.setStatus(OFFLINE_S);
                editor.putString("status",OFFLINE_S);
            }
        editor.apply();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (selected > 0) {
            menu.setGroupVisible(R.id.active, true);
            menu.setGroupVisible(R.id.standBy, false);
        } else if (selected == 0) {
            menu.setGroupVisible(R.id.active, false);
            menu.setGroupVisible(R.id.standBy, true);
        } else
            throw new IndexOutOfBoundsException("selected can't be lower than 0");
    }


    private void CallPhone() {
        //needs to check for recipient phone number, if it doesn't exist - display appropriate message
        if (selectedConversations.size() == 1) {
            String phoneNumber = selectedConversations.get(0).getRecipientPhoneNumber();
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
        UnSelectAll();
    }

    private void UnSelectAll() {
        ArrayList<Integer> selectedPosition = conversationsAdapter2.getSelectedPosition();
        while (selectedPosition.size() > 0) {
            int position = selectedPosition.get(0);
            if (recyclerView != null && recyclerView.findViewHolderForAdapterPosition(position) != null)
                Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(position)).itemView.performLongClick();
        }
    }

    private void BlockUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Action")
                .setMessage("Are you sure you would like to block this user? any message they will send will not arrive and all data will be deleted")
                .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_block_white, requireActivity().getTheme()))
                .setPositiveButton("Block", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!selectedConversations.isEmpty()) {
                            if (user != null) {
                                for (int i = 0; i < selectedConversations.size(); i++) {
                                    String userToBlock = selectedConversations.get(i).getRecipient();
                                    String conversationID = selectedConversations.get(i).getConversationID();
                                    BlockUser(userToBlock, conversationID);
                                }
                            }
                            requireActivity().invalidateOptionsMenu();
                            UnSelectAll();
                            Toast.makeText(requireContext(), "Blocked all selected users", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(requireContext(), "Will not block", Toast.LENGTH_SHORT).show();
                requireActivity().invalidateOptionsMenu();
            }
        }).setCancelable(true)
                .create()
                .show();
    }

    private void PinConversation() {
        if (selectedConversations.size() == 1)
            conversationsAdapter2.PinConversation(selectedConversations.get(0));
    }

    private void DataBaseSetUp() {
        dbActive = DBActive.getInstance(requireContext());
        //dbActive.ResetDB();
       /* dbActive.setListener(new DBActive.onResetDB() {
            @Override
            public void onReset() {
                conversationsAdapter2.Reset();
            }
        });*/
    }




    //called only if user doesn't exists - the first lunch of the app
    private void InsertUser(User user) {
        dbActive.checkIfUserExist(user);
    }

    //on each login, the user table is updated with the current login user
    private void UpdateUser(User user) {
        dbActive.updateUser(user);
    }

    //if the user exists in the database - load it, if not - download it from firebase database
    private void LoadCurrentUserFromDataBase() {
        user = dbActive.loadUserFromDataBase(currentUser);
        if(user!=null)
            callback.onLoadUserFromMemory(user);
        else
            controller.onDownloadUser(requireContext(), currentUser);
    }


    @Override
    public void onReceiveUser(User user) {
        if (user.getUserUID().equals(currentUser)) {//us - the user login
            InsertUser(user);
            this.user = user;
            //sends the data to mainActivity
            callback.onUserUpdate(user);
        }
    }


    private void UpdateConversationsInDataBase(Conversation conversation, boolean image) {

        dbActive.updateConversation(conversation);
        if (!image)
            conversationsAdapter2.updateConversation(conversation);

    }

    @Deprecated
    @Override
    public void onReceiveConversations(ArrayList<Conversation> conversations) {

    }

    //this method is only called on lunch or when a new conversation begins
    @Deprecated
    @Override
    public void onReceiveConversation(Conversation conversation) {
    }

    @Deprecated
    @Override
    public void onChangedConversation(Conversation conversation) {
    }

    @Deprecated
    @Override
    public void onRemoveConversation(Conversation conversation) {
    }

    @Override
    public void onReceiveUsersQuery(User user) {
        //callback.onUserQuery(user);
    }

    @Deprecated
    @Override
    public void onVersionChange(float newVersionNumber) {

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
                    HashMap<String, Object> tokenMap = new HashMap<>();
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        // TryToken tryToken = new TryToken(token);
                        tokenMap.put(currentUserUID, token);
                        controller.onUpdateData("Tokens", tokenMap);
                        //Server.updateServer("Tokens", tokenMap);
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
        controller.setMainGUI(null);

    }

    @Override
    public void onResume() {
        super.onResume();
        controller = CController.getController();
        controller.setMainGUI(this);
        ChangeStatus(ONLINE_S);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("New Conversation", MODE_PRIVATE);
        String newConversation = sharedPreferences.getString("new conversation", "no new conversation");
        if (!newConversation.equals("no new conversation")) {
            LoadNewConversation(newConversation);
            sharedPreferences.edit().putString("new conversation", "no new conversation").apply();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy fragment");
        controller.removeInterface(0);
        controller.setMainGUI(null);
        //controller.onUpdateData("users/" + currentUser + "/status", OFFLINE_S);

    }


    //---------------------------------------------------------------------------------------------------------------------------------------------//

    //calls all the functions needed to start the fragment
    private void init() {
        DataBaseSetUp();
        NullifyData();
        LoadCurrentUserFromDataBase();
        LoadConversations();
        TokenUpdate();
        onNewConversation();
        onUpdateConversation();
    }

    private void NullifyData() {
        SharedPreferences conversationPreferences = requireActivity().getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", "no conversation");
        editor.apply();
    }


    private void onNewConversation() {
        BroadcastReceiver newConversationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String newConversationID = intent.getStringExtra("conversationID");
                LoadNewConversation(newConversationID);

            }
        };
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(newConversationReceiver, new IntentFilter("New Conversation"));
    }

    private void onUpdateConversation() {
        BroadcastReceiver updateConversationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = (Message) intent.getSerializableExtra("message");
                //updates the conversation
                if (message != null) {
                    if(message.getArrivingTime() == null)
                    {
                        message.setArrivingTime(System.currentTimeMillis() + "");
                    }
                    MessageAction messageAction = message.getMessageAction();
                    if (messageAction == MessageAction.new_message) {
                        Conversation conversation = conversationsAdapter2.findConversation(message.getConversationID());//new Conversation(message.getConversationID());
                        conversation.setConversationMetaData(message);
                        UpdateConversationsInDataBase(conversation, false);
                        callback.onNewMessage(conversation.getConversationID());
                    }
                    if (messageAction == MessageAction.edit_message) {
                        //if the message to update is the last message in the conversation
                        if (conversationsAdapter2.getConversation(conversationsAdapter2.getItemCount() - 1).getLastMessageID().equals(message.getMessageID())) {
                            Conversation conversation = new Conversation(message.getConversationID());
                            conversation.setConversationMetaData(message);
                            UpdateConversationsInDataBase(conversation, false);
                        }
                    } else if (messageAction == MessageAction.delete_message) {
                        if (conversationsAdapter2.getConversation(conversationsAdapter2.getItemCount() - 1).getLastMessageID().equals(message.getMessageID())) {
                            Conversation conversation = new Conversation(message.getConversationID());
                            conversation.setConversationMetaData(message);
                            conversation.setLastMessage("message was deleted");
                            UpdateConversationsInDataBase(conversation, false);
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateConversationReceiver, new IntentFilter("Update Conversation"));
    }

    private void LoadNewConversation(String conversationID) {
        Conversation conversation = dbActive.getNewConversation(conversationID);
        conversationsAdapter2.setConversation(conversation,0);

    }

    //called when the fragment is lunched
    private void LoadConversations() {
        ArrayList<Conversation> list =(ArrayList<Conversation>) dbActive.getConversations();
        conversationsAdapter2.setConversations(list);

    }

    private void MuteConversation(String conversationID) {
        boolean mute = dbActive.muteConversation(conversationID);
        String dialog;
        if(mute)
            dialog = "Conversation was Muted";
        else
            dialog = "Conversation was unMuted";
        Snackbar.make(requireContext(), recyclerView, dialog, Snackbar.LENGTH_SHORT)
                .setAction("undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MuteConversation(conversationID);
                    }
                }).show();
        conversationsAdapter2.MuteConversation(conversationID,mute);
    }

    private void BlockUser(String uid, String conversationID) {
        boolean blocked =  dbActive.blockUser(uid);
        conversationsAdapter2.BlockConversation(blocked,conversationID);
    }

    private void DeleteConversation(String conversationID) {
        conversationsAdapter2.DeleteConversation(conversationID);
        dbActive.deleteConversation(conversationID);
    }

    public void RequestStatus()
    {
        for(Conversation conversation : conversations)
        {
            String token = conversation.getRecipientToken();
            Message message = new Message();
            message.setMessage(currentStatus);
            message.setMessageKind("requestStatus");
           // message.setMessageType(MessageType.requestStatus.ordinal());
            MessageSender sender = MessageSender.getInstance();
            sender.sendMessage(message,token);
        }
    }
}
