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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;


import android.os.Handler;
import android.provider.BaseColumns;
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
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import Adapters.ConversationsAdapter2;
import Consts.Tabs;
import Controller.CController;

import DataBase.DataBase;
import DataBase.DataBaseContract;
import NormalObjects.Conversation;

import NormalObjects.User;

import static com.example.woofmeow.MainActivity.OFFLINE_S;
import static com.example.woofmeow.MainActivity.ONLINE_S;
import static com.example.woofmeow.MainActivity.STANDBY_S;


@SuppressWarnings("Convert2Lambda")
public class TabFragment extends Fragment implements MainGUI {

    private static final String tabNumber = "tabNumber";
    private String currentUser;
    private ArrayList<Conversation> conversations = new ArrayList<>();
    private ArrayList<String>recipientsName = new ArrayList<>();
    private CController controller;
    private User user;
    private String currentStatus = ONLINE_S;
    private String link, title;
    private boolean openingActivity = false;
    private SQLiteDatabase db;
    private ConversationsAdapter2 conversationsAdapter2;
    private int selected = 0;
    private ArrayList<Conversation> selectedConversations = new ArrayList<>();
    private RecyclerView recyclerView;
    private String DATABASE_ERROR = "database error";
    private String FCM_ERROR = "fcm error";
    private boolean once = false;
    private View view;
    private LinearLayout searchLayout;
    private boolean search;


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
        //controller = new CController(this);
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
        controller.onDownloadUser(requireContext(), currentUser);
        TokenUpdate();
        setHasOptionsMenu(true);
        DataBaseSetUp();

        conversationsAdapter2 = new ConversationsAdapter2();
        ArrayList<Conversation> conversations = new ArrayList<>();
        conversationsAdapter2.setConversations(conversations);

        LoadUserFromDataBase();
        LoadConversationFromDataBase();
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
                        if (s.toString().equals(""))
                        {
                            conversationsAdapter2.Reset();
                        }
                        else {
                            int i = 0;
                            for (String name : recipientsName) {
                                if (!name.contains(s.toString())) {
                                    if(i<conversations.size()) {
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

                onSharedLinkIN();
                recyclerView.setAdapter(conversationsAdapter2);
                ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        //swiping left will promote to delete the conversation
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
                                            controller.onRemoveData("users/" + currentUser + "/conversations/" + conversation.getConversationID());
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
                            ArrayList<String> mutedUsers = user.getMutedUsersUID();
                            Conversation conversation = conversationsAdapter2.getConversation(viewHolder.getAdapterPosition());
                            if (mutedUsers.contains(conversation.getRecipient())) {
                                //unMute
                                Mute(false,conversation);
                                Snackbar.make(requireContext(),view,"User was unMuted",Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Mute(true,conversation);
                                    }
                                }).show();
                            } else {
                                //mute
                                Mute(true,conversation);
                                Snackbar.make(requireContext(),view,"User was muted",Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Mute(false,conversation);
                                    }
                                }).show();
                            }
                        }
                    }
                };
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
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
                        startConversationIntent.putExtra("conversationID",conversation.getConversationID());
                        startConversationIntent.putExtra("recipient",conversation.getRecipient());
                        startConversationIntent.putExtra("recipientPhone",conversation.getRecipientPhoneNumber());
                        startConversationIntent.putExtra("recipientImagePath",conversation.getRecipientImagePath());
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("share",Context.MODE_PRIVATE);
                        String title = sharedPreferences.getString("title","noTitle");
                        String link = sharedPreferences.getString("link","noLink");
                        if (!link.equals("noLink"))
                        {
                            startConversationIntent.putExtra("title",title);
                            startConversationIntent.putExtra("link",link);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("title");
                            editor.remove("link");
                            editor.apply();
                        }
                        requireActivity().startActivity(startConversationIntent);
                    }

                    @Override
                    public void onImageDownloaded(Conversation conversation,boolean image) {
                        UpdateConversationsInDataBase(conversation,image);
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
                        if(selected>0) {
                            UnSelectAll();
                            return true;
                        }
                        else return false;
                    }
                });


                break;
            }
            case groups:
                view = inflater.inflate(R.layout.grops_conversation_layout,container,false);
                break;

        }

        return view;
    }

    private void Mute(boolean mute,Conversation conversation) {

        if (mute) {
            controller.onUpdateData("users/" + currentUser + "/mutedUsers/" + conversation.getRecipient(), true);
            controller.onUpdateData("users/" + currentUser + "/conversations/" + conversation.getConversationID() + "/conversationInfo/muted", true);
        } else {
            controller.onRemoveData("users/" + currentUser + "/mutedUsers/" + conversation.getRecipient());
            controller.onUpdateData("users/" + currentUser + "/conversations/" + conversation.getConversationID() + "/conversationInfo/muted", false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.pinConversation:
                PinConversation();
                break;
            case R.id.callBtn:
                if (selected == 1)
                    CallPhone();
                break;

            case R.id.block:
                BlockUser();
                break;
            case R.id.searchConversation:{
                Animation in = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down);
                Animation out = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up);
                if (searchLayout.getVisibility() == View.GONE)
                {
                    searchLayout.setVisibility(View.VISIBLE);
                    searchLayout.startAnimation(in);
                    recyclerView.startAnimation(in);
                    conversationsAdapter2.SetBackUp();
                    search = true;
                }
                else
                {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            searchLayout.setVisibility(View.GONE);
                        }
                    },out.getDuration());
                    searchLayout.startAnimation(out);
                    recyclerView.startAnimation(out);
                    conversationsAdapter2.Reset();
                    search = false;
                }
                break;
            }
            case R.id.status:
                switch (currentStatus) {
                    case ONLINE_S:
                        item.setIcon(R.drawable.circle_red);
                        currentStatus = OFFLINE_S;
                        ChangeStatus(OFFLINE_S);
                        //controller.onUpdateData("users/" + currentUser + "/status", OFFLINE_S);
                        break;
                    case OFFLINE_S:
                        item.setIcon(R.drawable.circle_yellow);
                        currentStatus = STANDBY_S;
                        ChangeStatus(STANDBY_S);
                        // controller.onUpdateData("users/" + currentUser + "/status", STANDBY_S);
                        break;
                    case STANDBY_S:
                        item.setIcon(R.drawable.circle_green);
                        currentStatus = ONLINE_S;
                        ChangeStatus(ONLINE_S);
                        // controller.onUpdateData("users/" + currentUser + "/status", ONLINE_S);
                        break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ChangeStatus(String currentStatus) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean status = preferences.getBoolean("status", true);
        if (status)
        {
            if (user!=null) {
                user.setStatus(currentStatus);
                UpdateUser(user);
            }
            controller.onUpdateData("users/" + currentUser + "/status", currentStatus);
        }
        else
        {
            if (user!=null) {
                user.setStatus(OFFLINE_S);
                UpdateUser(user);
            }
            controller.onUpdateData("users/" + currentUser + "/status", OFFLINE_S);
        }
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
                                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("blocked",Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                for (int i = 0; i < selectedConversations.size(); i++) {
                                    String userToBlock = selectedConversations.get(i).getRecipient();
                                    String conversationID = selectedConversations.get(i).getConversationID();
                                    controller.onUpdateData("users/" + currentUser + "/blocked/" + userToBlock, true);
                                    controller.onUpdateData("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/blocked",true);
                                    editor.putString(userToBlock,userToBlock);
                                    editor.apply();

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

    private void BlockUserLocally(String userToBlock)
    {
        if (db!=null)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataBaseContract.BlockedUsers.USER_UID,userToBlock);
            long rawNumber =  db.insert(DataBaseContract.BlockedUsers.BLOCKED_USERS_TABLE,null,contentValues);
            if (rawNumber == -1)
                Log.e(DATABASE_ERROR,"didn't update database with new blocked user");
        }
    }

    private void PinConversation() {
        if (selectedConversations.size() == 1)
            conversationsAdapter2.PinConversation(selectedConversations.get(0));
    }

    private void DataBaseSetUp() {
        if (db == null) {
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


            DataBase dbHelper = new DataBase(requireContext());
            db = dbHelper.getWritableDatabase();
            // dbHelper.onUpgrade(db,db.getVersion(),db.getVersion()+1);
             //db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Entry.CONVERSATIONS_TABLE);
            //db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Messages.MESSAGES_TABLE);
            //db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.User.USER_TABLE);
            // dbHelper.onDowngrade(db,db.getVersion(),db.getVersion()-1);
            dbHelper.onUpgrade(db, db.getVersion(), db.getVersion() + 1);
            //db.execSQL("CREATE TABLE IF NOT EXISTS " + DataBaseContract.Entry.CONVERSATIONS_TABLE);
        }
    }

    //called only if user doesn't exists - the first lunch of the app
    private void InsertUser(User user) {
        ContentValues values = CreateUserValues(user);
        long rowID = db.insert(DataBaseContract.User.USER_TABLE, null, values);
        if (rowID == -1)
            Log.e(DATABASE_ERROR, "error inserting user to database");
    }

    //on each login, the user table is updated with the current login user
    private void UpdateUser(User user) {
        ContentValues values = CreateUserValues(user);
        int count = db.update(DataBaseContract.User.USER_TABLE, values, null, null);
        if (count != 1)
            Log.e(DATABASE_ERROR, "more than 1 or 0 rows were updated in the user table");
    }

    private ContentValues CreateUserValues(User user)
    {
        ContentValues values = new ContentValues();
        values.put(DataBaseContract.User.USER_UID, user.getUserUID());
        values.put(DataBaseContract.User.USER_NAME, user.getName());
        values.put(DataBaseContract.User.USER_LAST_NAME, user.getLastName());
        values.put(DataBaseContract.User.USER_TIME_CREATED, user.getTimeCreated());
        values.put(DataBaseContract.User.USER_PICTURE_LINK, user.getPictureLink());
        if (user.getPhoneNumber() != null)
            values.put(DataBaseContract.User.USER_PHONE_NUMBER,user.getPhoneNumber());
        values.put(DataBaseContract.User.USER_LAST_STATUS,user.getStatus());
        return values;
    }
    /*
        we check if the user exists in the database. compare user uid to the saved uid. unless the table is empty just update the user table with the new user that login
        then we load the conversations assigned to this user uid
     */
    private void CheckIfUserExist(User user) {
        if (db != null) {
            String[] projections = {
                    DataBaseContract.User._ID,
                    DataBaseContract.User.USER_UID
            };
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, null, null, null, null, null);
            if (cursor.getCount() > 1)
                Log.e(DATABASE_ERROR, "cursor contains more than 1 user");
            else if (cursor.moveToNext())
                UpdateUser(user);
            else
                InsertUser(user);
            cursor.close();
        }
    }

    private void LoadUserFromDataBase() {
        if (db != null) {
            String[] projections = {
                    DataBaseContract.User._ID,
                    DataBaseContract.User.USER_UID,
                    DataBaseContract.User.USER_NAME,
                    DataBaseContract.User.USER_LAST_NAME,
                    DataBaseContract.User.USER_PICTURE_LINK,
                    DataBaseContract.User.USER_TIME_CREATED,
                    DataBaseContract.User.USER_PHONE_NUMBER,
                    DataBaseContract.User.USER_LAST_STATUS
            };
            String selection = DataBaseContract.User.USER_UID + " LIKE ?";
            String[] selectionArgs = {currentUser};
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, selection, selectionArgs, null, null, null);
            cursor.moveToNext();
            if (cursor.getCount() == 1) {
                User user = new User();
                String uid = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_UID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_NAME));
                String pictureLink = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_PICTURE_LINK));
                String timeCreated = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_TIME_CREATED));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_PHONE_NUMBER));
                String status = cursor.getColumnName(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_STATUS));
                user.setUserUID(uid);
                user.setName(name);
                user.setLastName(lastName);
                user.setPictureLink(pictureLink);
                user.setTimeCreated(timeCreated);
                user.setPhoneNumber(phoneNumber);
                user.setStatus(status);
                this.user = user;
                callback.onLoadUserFromMemory(user);
            } else if (cursor.getCount() > 1)
                Log.e(DATABASE_ERROR, "cursor contains more than 1 user");
            else
                Log.e(DATABASE_ERROR, "no user in database ");
            cursor.close();
        } else
            Log.e(DATABASE_ERROR, "db is null");
    }


    @Override
    public void onReceiveUser(User user) {
        if (user.getUserUID().equals(currentUser)) {//us - the user login
            CheckIfUserExist(user);
            this.user = user;
            //since user is being called for each message sent, and inorder not to have repeating conversations, once variable prevents it
            if (!once)
            {
                controller.onDownloadConversations(requireContext());
                once = true;
            }
            //sends the data to mainActivity
            callback.onUserUpdate(user);
            //asking for recipients User
            /*for(int i = 0;i<conversations.size();i++)
                controller.onDownloadUser(requireContext(),conversations.get(i).getRecipient());*/
        }
        else // the recipients
        {
            System.out.println("got recipients");
            //assignRecipientsToConversations(user);
        }
    }



    private void InsertConversationToDataBase(Conversation conversation) {
        String conversationID = conversation.getConversationID();

        ContentValues values = new ContentValues();
        values.put(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME, conversationID);
        values.put(DataBaseContract.Entry.CONVERSATIONS_MUTE_COLUMN_NAME, conversation.isMuted());
        values.put(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_COLUMN_NAME, conversation.getLastMessage());
        values.put(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME, conversation.getLastMessageTime());
        values.put(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME, conversation.getMessageType());
        values.put(DataBaseContract.Entry.CONVERSATION_RECIPIENT, conversation.getRecipient());
        values.put(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_ID, conversation.getLastMessageID());
        values.put(DataBaseContract.Entry.CONVERSATION_RECIPIENT_IMAGE_PATH,conversation.getRecipientImagePath());
        values.put(DataBaseContract.Entry.CONVERSATION_RECIPIENT_NAME,conversation.getSenderName());
        values.put(DataBaseContract.Entry.USER_UID, user.getUserUID());

        //  values.put(DataBaseContract.Entry.CONVERSATIONS_BLOCK_COLUMN_NAME,"CONVERSATION_BLOCKED");

        long newRowId = db.insert(DataBaseContract.Entry.CONVERSATIONS_TABLE, null, values);
        if (newRowId == -1)
            Log.e(DATABASE_ERROR, "error inserting data to database");
        else {
            conversationsAdapter2.addConversation(conversation);
            conversations.add(conversation);
            //conversationsAdapter2.SetBackUp();
        }
    }

    private void DeleteFromDataBase(Conversation conversation) {
        String selection = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = {conversation.getConversationID()};
        int deletedRows = db.delete(DataBaseContract.Entry.CONVERSATIONS_TABLE, selection, selectionArgs);
        if (deletedRows == -1)
            Log.e(DATABASE_ERROR, "didn't delete anything - deleted rows = -1");

    }

    private void UpdateConversationsInDataBase(Conversation conversation,boolean image) {

        DataBaseSetUp();
        ContentValues values = new ContentValues();
        values.put(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME, conversation.getConversationID());
        values.put(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_ID, conversation.getLastMessageID());
        values.put(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME, conversation.getMessageType());
        values.put(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_COLUMN_NAME, conversation.getLastMessage());
        values.put(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME, conversation.getLastMessageTime());
        values.put(DataBaseContract.Entry.CONVERSATION_RECIPIENT_IMAGE_PATH,conversation.getRecipientImagePath());
        values.put(DataBaseContract.Entry.CONVERSATION_RECIPIENT_NAME,conversation.getSenderName());
        values.put(DataBaseContract.Entry.CONVERSATIONS_MUTE_COLUMN_NAME, conversation.isMuted());
        values.put(DataBaseContract.Entry.USER_UID, user.getUserUID());
        String selection = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = {conversation.getConversationID()};
        int count = db.update(DataBaseContract.Entry.CONVERSATIONS_TABLE, values, selection, selectionArgs);
        if (count > 0)
            if (!image)
                conversationsAdapter2.updateConversation(conversation);

    }

    private void PrintDataBase() {
        if (db != null) {
            String[] projections = {
                    BaseColumns._ID,
                    DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME,
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_COLUMN_NAME,
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME,
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME,
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_ID,
                    DataBaseContract.Entry.CONVERSATION_RECIPIENT
                    // DataBaseContract.Entry.CONVERSATIONS_MUTE_COLUMN_NAME,
                    //DataBaseContract.Entry.CONVERSATIONS_BLOCK_COLUMN_NAME
            };
            //String sortOrder = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " ASC";
            Cursor cursor = db.query(DataBaseContract.Entry.CONVERSATIONS_TABLE, projections, null, null, null, null, null);
            List<String> conversationIDsList = new ArrayList<>();
            List<String> lastMessagesList = new ArrayList<>();
            List<String> lastMessagesTimeList = new ArrayList<>();
            List<Integer> lastMessageTypeList = new ArrayList<>();
            List<String> recipientList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String conversationIDs = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME));
                conversationIDsList.add(conversationIDs);
                String lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_COLUMN_NAME));
                lastMessagesList.add(lastMessage);
                String lastMessageTime = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME));
                lastMessagesTimeList.add(lastMessageTime);
                int lastMessageType = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME));
                lastMessageTypeList.add(lastMessageType);
                String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_RECIPIENT));
                recipientList.add(recipient);
            }
            cursor.close();
            System.out.println(conversationIDsList);
            System.out.println(lastMessagesList);
            System.out.println(lastMessagesTimeList);
            System.out.println(lastMessageTypeList);
            System.out.println(recipientList);
        }
    }

    //idType - true for conversationID, false for messageID
    private boolean CheckIfExist(String ID, boolean idType) {
        if (ID != null) {
            if (db != null) {
                String[] projections = {
                        BaseColumns._ID,
                        DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME,
                        DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_ID
                };
                //String sortOrder = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " DESC";
                String selections = DataBaseContract.Entry.USER_UID + " LIKE ?";
                String[] selectionArgs = {user.getUserUID()};
                Cursor cursor = db.query(DataBaseContract.Entry.CONVERSATIONS_TABLE, projections, selections, selectionArgs, null, null, null);
                while (cursor.moveToNext()) {
                    if (idType) {

                        String IDs = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME));
                        if (IDs.equals(ID)) {
                            cursor.close();
                            return true;
                        }
                    } else {
                        String IDs = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_ID));
                        if (IDs.equals(ID)) {
                            cursor.close();
                            return true;
                        }
                    }
                }


                cursor.close();
            }
        }else
            Log.e(DATABASE_ERROR,"id is null");
        return false;
    }

    private void LoadConversationFromDataBase() {
        if (db != null) {

            String[] projections = {
                    BaseColumns._ID,
                    DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME,
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_COLUMN_NAME,
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME,
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME,
                    DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_ID,
                    DataBaseContract.Entry.CONVERSATION_RECIPIENT,
                    DataBaseContract.Entry.CONVERSATIONS_MUTE_COLUMN_NAME,
                    DataBaseContract.Entry.USER_UID,
                    DataBaseContract.Entry.CONVERSATION_RECIPIENT_NAME,
                    DataBaseContract.Entry.CONVERSATION_RECIPIENT_IMAGE_PATH
                    //DataBaseContract.Entry.CONVERSATIONS_BLOCK_COLUMN_NAME
            };
            String selection = DataBaseContract.Entry.USER_UID + " LIKE ?";
            if (user != null && user.getUserUID() != null) {
                String[] selectionArgs = {user.getUserUID()};
                Cursor cursor = db.query(DataBaseContract.Entry.CONVERSATIONS_TABLE, projections, selection, selectionArgs, null, null, null);
                while (cursor.moveToNext()) {
                    String conversationIDs = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME));
                    String lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_COLUMN_NAME));
                    String lastMessageTime = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME));
                    int lastMessageType = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME));
                    String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_RECIPIENT));
                    String lastMessageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_LAST_MESSAGE_ID));
                    String recipientName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_RECIPIENT_NAME));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATION_RECIPIENT_IMAGE_PATH));
                    Conversation conversation = new Conversation(conversationIDs);
                    conversation.setLastMessageTimeFormatted(lastMessageTime);
                    conversation.setLastMessage(lastMessage);
                    conversation.setMessageType(lastMessageType);
                    conversation.setLastMessageID(lastMessageID);
                    conversation.setRecipient(recipient);
                    conversation.setRecipientImagePath(imagePath);
                    conversation.setSenderName(recipientName);
                    conversationsAdapter2.addConversation(conversation);
                   // conversationsAdapter2.setRecipientName(recipientName);
                    recipientsName.add(recipientName);
                    conversations.add(conversation);
                }
                //conversationsAdapter2.SetBackUp();
                cursor.close();
            }
        }
    }

    @Override
    public void onReceiveConversations(ArrayList<Conversation> conversations) {

    }

    //this method is only called on lunch or when a new conversation begins
    @Override
    public void onReceiveConversation(Conversation conversation) {
        //checks if conversation already exists
        if(!CheckIfExist(conversation.getConversationID(),true))
        {
            //if conversation doesn't exists - meaning its a new conversation
            InsertConversationToDataBase(conversation);
        }
        else
        {
            //meaning conversationExists already - so we only need to update the database
            UpdateConversationsInDataBase(conversation,false);
        }
    }

    @Override
    public void onChangedConversation(Conversation conversation) {
        UpdateConversationsInDataBase(conversation,false);
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
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("App",Context.MODE_PRIVATE);
        float currentVersionNumber = sharedPreferences.getFloat("Version",-1);
        if (currentVersionNumber != newVersionNumber)
        {
            Snackbar.make(requireContext(),view,"A newer version is available",Snackbar.LENGTH_SHORT).setAction("Update", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //opens play store with the app link
                }
            });
        }
    }


    private void TokenUpdate() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful())
                    Log.e(FCM_ERROR,"Fetching FCM registration token failed: " + task.getException());
                else {
                    String token = task.getResult();
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


    private void assignRecipientsToConversations(User user) {
        String userID = user.getUserUID();
        for (int i = 0; i < conversations.size(); i++) {
            if (userID.equals(conversations.get(i).getRecipient())) {
                conversations.get(i).setRecipientPhoneNumber(user.getPhoneNumber());
                ContentValues contentValues = new ContentValues();
                System.out.println(user.getName());
                contentValues.put(DataBaseContract.Entry.CONVERSATION_RECIPIENT_NAME, user.getName());
                String selection = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " LIKE ?";
                String[] selectionArgs = {conversations.get(i).getConversationID()};
                int k = db.update(DataBaseContract.Entry.CONVERSATIONS_TABLE, contentValues, selection, selectionArgs);
                if (k != 1)
                    Log.e(DATABASE_ERROR, "updated more than 1 conversation in assignRecipientsToConversation");
               // conversationsAdapter2.setRecipientName(user.getName());
                //recipientsName.add(user.getName());
                break;
            }
        }
    }

    private String checkOverriddenPhoneNumber(int key) {
        String recipientUID = conversations.get(key).getRecipient();
        return user.getRecipientPhoneNumber(recipientUID);
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
        controller.onRemoveUserListener();
        controller.setMainGUI(null);

    }

    @Override
    public void onResume() {
        super.onResume();
        controller = CController.getController();
        controller.setMainGUI(this);
        ChangeStatus(ONLINE_S);
        ArrayList<Conversation>waitingConversations =  controller.getWaitingConversations();
        if (!waitingConversations.isEmpty())
        {
            for (Conversation conversation : waitingConversations)
            {
                UpdateConversationsInDataBase(conversation,false);
            }
            controller.ResetWaitingConversations();
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

}
