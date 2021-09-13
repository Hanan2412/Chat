package Fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.text.Editable;
import android.text.TextWatcher;
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

import DataBase.DataBaseContract;
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
    private ArrayList<String> recipientsName = new ArrayList<>();
    private CController controller;
    private User user;
    private String currentStatus = ONLINE_S;
    private String link, title;
    private boolean openingActivity = false;
    private ConversationsAdapter2 conversationsAdapter2;
    private int selected = 0;
    private ArrayList<Conversation> selectedConversations = new ArrayList<>();
    private RecyclerView recyclerView;
    private final String FCM_ERROR = "fcm error";

    private View view;
    private LinearLayout searchLayout;
    private boolean search;

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

        void onConversationAction();

        void onUserQuery(User user);

        void onNewMessage(boolean group);
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
        //controller.onDownloadUser(requireContext(), currentUser);
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
                searchQuery.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.toString().equals("")) {
                            conversationsAdapter2.Reset();
                        } else {
                            int i = 0;
                            for (String name : recipientsName) {
                                if (!name.contains(s.toString())) {
                                    if (i < conversations.size()) {
                                        conversationsAdapter2.deleteConversation(conversations.get(i));
                                        conversationsAdapter2.notifyItemRemoved(i);
                                    }
                                }
                                i++;
                            }
                        }
                    }
                });
                searchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String searchThis = searchQuery.getText().toString();
                        Toast.makeText(requireContext(), "searching this: " + searchThis, Toast.LENGTH_SHORT).show();
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
                        User recipient = dbActive.LoadUserFromDataBase(conversation.getRecipient());
                        Intent startConversationIntent = new Intent(requireActivity(), ConversationActivity.class);
                        startConversationIntent.putExtra("conversation",conversation);
                        startConversationIntent.putExtra("conversationID", conversation.getConversationID());
                        startConversationIntent.putExtra("recipient", conversation.getRecipient());
                        startConversationIntent.putExtra("recipientPhone", conversation.getRecipientPhoneNumber());
                        startConversationIntent.putExtra("recipientImagePath", conversation.getRecipientImagePath());
                        startConversationIntent.putExtra("recipientToken",conversation.getRecipientToken());
                        startConversationIntent.putExtra("recipientUser",recipient);
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
            case groups:
                view = inflater.inflate(R.layout.coming_soon_layout, container, false);
                break;
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
                conversationsAdapter2.SetBackUp();
                search = true;
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchLayout.setVisibility(View.GONE);
                    }
                }, out.getDuration());
                searchLayout.startAnimation(out);
                recyclerView.startAnimation(out);
                conversationsAdapter2.Reset();
                search = false;
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
        boolean status = preferences.getBoolean("status", true);
        if (status) {
            if (user != null) {
                user.setStatus(currentStatus);
                UpdateUser(user);
            }
            editor.putString("status", currentStatus);
            //controller.onUpdateData("users/" + currentUser + "/status", currentStatus);
        } else {
            if (user != null) {
                user.setStatus(OFFLINE_S);
                UpdateUser(user);
            }
            editor.putString("status", OFFLINE_S);
            //controller.onUpdateData("users/" + currentUser + "/status", OFFLINE_S);
        }
        editor.apply();
    }

    private String getMyToken() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Token", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "no token");
        if (!token.equals("no token"))
            return token;
        else
            Log.e("TOKEN ERROR", "no token for current user");
        return null;
    }

    public String[] getRecipientsList() {
        //tmp implementation
        String[] recipientsTokens = new String[1];
        recipientsTokens[0] = "eZ6xrJiQQNWGIPAZhSpVK6:APA91bFLWs47d-xN_-FGVtx5ixyCEkTczIycjnjO5AIEKh2aDlGbnq4MDwPlJYp8s1juJyQb9y4Wx3l09XIA5Hl9GqWE85hXafwcao7Op0uGqoNxdGtH61ri--Td9Jwu5SU0oKUHe07L";
        return recipientsTokens;
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
       // dbActive.ResetDB();
       // if (db == null) {

            /*DataBase dbHelper = new DataBase(requireContext());
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Callable<SQLiteDatabase>callable = new Callable<SQLiteDatabase>() {
                @Override
                public SQLiteDatabase call() throws Exception {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    dbHelper.onUpgrade(db, db.getVersion(), db.getVersion() + 1);
                    return db;
                }
            };
            Future<SQLiteDatabase> future = executorService.submit(callable);
            try {
                db = future.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            executorService.shutdown();*/


          /*  DataBase dbHelper = new DataBase(requireContext());
            db = dbHelper.getWritableDatabase();*/
            // dbHelper.onUpgrade(db,db.getVersion(),db.getVersion()+1);
           /* db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Conversations.CONVERSATIONS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Messages.MESSAGES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.User.USER_TABLE);*/
            // dbHelper.onDowngrade(db,db.getVersion(),db.getVersion()-1);
           // dbHelper.onUpgrade(db, db.getVersion(), db.getVersion() + 1);
            //db.execSQL("CREATE TABLE IF NOT EXISTS " + DataBaseContract.Conversations.CONVERSATIONS_TABLE);
        //}
    }

    //called only if user doesn't exists - the first lunch of the app
    private void InsertUser(User user) {
        dbActive.CheckIfUserExist(user);

    }

    //on each login, the user table is updated with the current login user
    private void UpdateUser(User user) {
        dbActive.UpdateUser(user);

    }

    //if the user exists in the database - load it, if not - download it from firebase database
    private void LoadCurrentUserFromDataBase() {
        user = dbActive.LoadUserFromDataBase(currentUser);
        if(user!=null)
            callback.onLoadUserFromMemory(user);
        else
            controller.onDownloadUser(requireContext(), currentUser);
    }


    @Override
    public void onReceiveUser(User user) {
        if (user.getUserUID().equals(currentUser)) {//us - the user login
           // CheckIfUserExist(user);
            dbActive.CheckIfUserExist(user);
            this.user = user;
            //sends the data to mainActivity
            callback.onUserUpdate(user);
        }
    }


    private void InsertConversationToDataBase(Conversation conversation) {
        dbActive.InsertConversationToDataBase(conversation);
        conversationsAdapter2.addConversation(conversation);
        conversations.add(conversation);
    }

    private void UpdateConversationsInDataBase(Conversation conversation, boolean image) {

        dbActive.UpdateConversation(conversation);
        if (!image)
            conversationsAdapter2.updateConversation(conversation);

    }


    //idType - true for conversationID, false for messageID
    private boolean CheckIfExist(String ID, boolean idType) {
       return dbActive.CheckIfExist(ID,idType);
    }


    @Deprecated
    @Override
    public void onReceiveConversations(ArrayList<Conversation> conversations) {

    }

    //this method is only called on lunch or when a new conversation begins
    @Override
    public void onReceiveConversation(Conversation conversation) {
        //checks if conversation already exists
        if (!CheckIfExist(conversation.getConversationID(), true)) {
            //if conversation doesn't exists - meaning its a new conversation
            InsertConversationToDataBase(conversation);
        } else {
            //meaning conversationExists already - so we only need to update the database
            UpdateConversationsInDataBase(conversation, false);
        }
    }

    @Override
    public void onChangedConversation(Conversation conversation) {
        UpdateConversationsInDataBase(conversation, false);
    }

    @Override
    public void onRemoveConversation(Conversation conversation) {
        conversationsAdapter2.deleteConversation(conversation);
    }

    @Override
    public void onReceiveUsersQuery(User user) {
        callback.onUserQuery(user);
    }

    @Override
    public void onVersionChange(float newVersionNumber) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("App", MODE_PRIVATE);
        float currentVersionNumber = sharedPreferences.getFloat("Version", -1);
        if (currentVersionNumber != newVersionNumber) {
            Snackbar.make(requireContext(), view, "A newer version is available", Snackbar.LENGTH_SHORT).setAction("Update", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //opens play store with the app link
                }
            }).show();
        }
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

    private void onSharedLinkIN() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                title = intent.getStringExtra("title");
                link = intent.getStringExtra("link");

            }
        };
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, new IntentFilter("sharedDataIN"));
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------//

    //calls all the functions needed to start the fragment
    private void init() {
        DataBaseSetUp();
        NullifyData();
        //LoadCurrentUserID();
        LoadCurrentUserFromDataBase();
        LoadConversations();
        TokenUpdate();
        onNewConversation();
        onUpdateConversation();
        onSharedLinkIN();
    }

    private void NullifyData() {
        SharedPreferences conversationPreferences = requireActivity().getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", "no conversation");
        editor.apply();
    }

    private void LoadCurrentUserID() {
        String currentUser;
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CurrentUser", MODE_PRIVATE);
        currentUser = sharedPreferences.getString("currentUser", "no user");
        if (!currentUser.equals("no user"))
            this.currentUser = currentUser;
        else {
            this.currentUser = "sDhl6ueP20WwMTKJW3fKK5FK5Nk2";//emulator
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("currentUser", this.currentUser);
            editor.apply();
            //this.currentUser = "pfghXKWGCja8i8YPQz71DuXxyTI2";//phone
            Toast.makeText(requireContext(), "error fetching user information", Toast.LENGTH_SHORT).show();
        }
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
                        Conversation conversation = new Conversation(message.getConversationID());
                        conversation.setConversationMetaData(message);
                        callback.onNewMessage(false);
                        UpdateConversationsInDataBase(conversation, false);
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


    private void UpdateConversation(String conversationID) {
        Message lastMessage = RetrieveLastMessage(conversationID);
        conversationsAdapter2.UpdateConversation(lastMessage, conversationID);
    }

    //called when the fragment is lunched to update conversation view
    private Message RetrieveLastMessage(String conversationID) {
        String selection = DataBaseContract.Conversations.CONVERSATION_ID + " = ?";
        return RetrieveMessage(conversationID, selection);
    }

    //retrieves the last message in a conversation or a message by its id
    private Message RetrieveMessage(String id, String selection) {
        return dbActive.RetrieveMessage(id,selection);

    }

    private void MuteConversation(String conversationID) {
        boolean muted = conversationsAdapter2.MuteConversation(conversationID);
        dbActive.Mute(conversationID,muted);
        if (muted)
            Snackbar.make(requireContext(), recyclerView, "Conversation Was Muted", Snackbar.LENGTH_SHORT)
                    .setAction("undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MuteConversation(conversationID);
                        }
                    }).show();
        else
            Snackbar.make(requireContext(), recyclerView, "Conversation was unMuted", Snackbar.LENGTH_SHORT)
                    .setAction("undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MuteConversation(conversationID);
                        }
                    }).show();

    }

    private void BlockUser(String uid, String conversationID) {
        boolean blocked = conversationsAdapter2.BlockedConversation(conversationID);
        dbActive.Block(uid,conversationID,blocked);
    }

    private void DeleteConversation(String conversationID) {
        conversationsAdapter2.DeleteConversation(conversationID);
        dbActive.DeleteConversation(conversationID);

    }
}
