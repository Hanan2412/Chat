package Fragments;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.DropDownPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.woofmeow.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.TimeUnit;

//import Controller.CController;
import Backend.ChatDao;
import Backend.ChatDataBase;
import Backend.WorkScheduler;
import NormalObjects.Conversation;
import NormalObjects.Message;
import Retrofit.Server;

@SuppressWarnings({"Convert2Lambda", "ConstantConditions"})
public class SecondPagePreferenceFragment extends PreferenceFragmentCompat {

    private final String CHAT_PREFERENCE = "ChatPreference";
    private final String ACCOUNT_PREFERENCES = "AccountPreference";
    private final String NOTIFICATIONS_PREFERENCES = "NotificationsPreference";
    private final String BACKUP_PREFERENCE = "backup";
    private final String BACKUP_WORKER = "backup";
    private final String DEBUG_MODE = "debug";
    private boolean start = true;
    private final int CALL_PHONE = 7;
    //private CController controller;
    private final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private SwitchPreferenceCompat phoneCall;
    private final String preferencePath = "users/" + currentUser + "/preferences/";
    @SuppressLint("RestrictedApi")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
       /* controller = CController.getController();
        controller.setPreferencesInterface(this);*/
        Bundle bundle = getArguments();
        if (bundle != null) {
            String key = bundle.getString("key", "noKey");
            switch (key) {
                case CHAT_PREFERENCE:
                    setPreferencesFromResource(R.xml.chat_preference, rootKey);
                    ListPreference textSize = findPreference("textSize");
                    textSize.setSummary("Choose the text size your texts will be at.\ncurrent size: " + textSize.getValue());
                    textSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            ListPreference textSize = (ListPreference) preference;
                            textSize.setSummary("Choose the text size your texts will be at.\ncurrent size: " + newValue);
                            textSize.setValue((String) newValue);
                            return false;
                        }
                    });
                    phoneCall = findPreference("PhoneCall");
                    if (requireContext().checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                        phoneCall.setChecked(true);
                    else
                        phoneCall.setChecked(false);
                    phoneCall.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            int hasCallPermission = requireContext().checkSelfPermission(Manifest.permission.CALL_PHONE);
                            if (hasCallPermission != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE);
                            } else
                                phoneCall.setChecked(true);
                            return false;
                        }
                    });
                    ListPreference actionList = findPreference("ActionButton");
                    actionList.setSummary("action button set to: " + actionList.getValue());
                    actionList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            actionList.setSummary("action button set to: " + newValue);
                            actionList.setValue((String) newValue);
                            return false;
                        }
                    });
                    break;

                case ACCOUNT_PREFERENCES: {
                    setPreferencesFromResource(R.xml.account_preference, rootKey);
                    Preference deleteAllData = findPreference("deleteAllConversations");
                    deleteAllData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setCancelable(true)
                                    .setPositiveButton("Delete my conversations", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(requireContext(), "Deleting data...", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(requireContext(), "cancelled, will not delete data", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }).setMessage("Are you sure you want to delete all of your data?")
                                    .setTitle("Confirm Action")
                                    .create()
                                    .show();
                            return false;
                        }
                    });
                    Preference deleteAccount = findPreference("deleteAccount");
                    deleteAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle("Confirm Action")
                                    .setMessage("Are you sure you want to delete your account?")
                                    .setPositiveButton("yes, delete my account", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Server.getInstance().deleteUser(currentUser);
                                            //Server3.getInstance().deleteData("users/" + currentUser);
                                            //controller.onPreferenceDelete("users/" + currentUser);
                                            Toast.makeText(requireContext(), "deleting account", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton("no, i don't want to delete my account", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setCancelable(true)
                                    .create()
                                    .show();

                            return false;
                        }
                    });
                    break;
                }
                case NOTIFICATIONS_PREFERENCES:{
                    setPreferencesFromResource(R.xml.notification_preferences,rootKey);
                    break;
                }
                case BACKUP_PREFERENCE:
                    setPreferencesFromResource(R.xml.backup_preference,rootKey);
                    DropDownPreference backupTime = findPreference("backupTime");
                    backupTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            if (!start) {
                                String[] options = getResources().getStringArray(R.array.backupTime);
                                Constraints constraints = new Constraints.Builder()
                                        .setRequiresBatteryNotLow(true)
                                        .setRequiredNetworkType(NetworkType.CONNECTED)
                                        .build();
                                WorkRequest workRequest = null;
                                switch (options[Integer.parseInt((String) newValue)]) {
                                    case "daily":
                                        workRequest = new PeriodicWorkRequest.Builder(WorkScheduler.class, 1, TimeUnit.DAYS)
                                                .setConstraints(constraints)
                                                .addTag(BACKUP_WORKER)
                                                .build();
                                        break;
                                    case "weekly":
                                        workRequest = new PeriodicWorkRequest.Builder(WorkScheduler.class, 7, TimeUnit.DAYS)
                                                .setConstraints(constraints)
                                                .addTag(BACKUP_WORKER)
                                                .build();
                                        break;
                                    case "monthly":
                                        workRequest = new PeriodicWorkRequest.Builder(WorkScheduler.class, 30, TimeUnit.DAYS)
                                                .setConstraints(constraints)
                                                .addTag(BACKUP_WORKER)
                                                .build();
                                        break;
                                }
                                SwitchPreferenceCompat wifiOnly = findPreference("wifiOnly");
                                if (workRequest != null)
                                    if (wifiOnly.isChecked()) {
                                        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
                                            WorkManager.getInstance(requireContext()).enqueue(workRequest);
                                        }
                                    } else {
                                        WorkManager.getInstance(requireContext()).enqueue(workRequest);
                                    }
                            }
                            start = false;
                            return false;
                        }
                    });
                    Preference backupNow = findPreference("backupNow");
                    backupNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Constraints constraints = new Constraints.Builder()
                                    .setRequiresBatteryNotLow(true)
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build();
                            WorkRequest workRequest =  new OneTimeWorkRequest.Builder(WorkScheduler.class)
                                    .setConstraints(constraints)
                                    .addTag(BACKUP_WORKER)
                                    .build();
                            SwitchPreferenceCompat wifiOnly = findPreference("wifiOnly");
                            if (wifiOnly.isChecked())
                            {
                                ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(CONNECTIVITY_SERVICE);
                                if (connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI)
                                {
                                    WorkManager.getInstance(requireContext()).enqueue(workRequest);
                                }
                            }
                            else
                                WorkManager.getInstance(requireContext()).enqueue(workRequest);
                            return false;
                        }
                    });
                    Preference restore = findPreference("restore");
                    restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            ChatDataBase db = ChatDataBase.getInstance(requireContext());
                            ChatDao dao = db.chatDao();
                            Server server = Server.getInstance();
                            server.setRestoreListener(new Server.onRestoreListener() {
                                @Override
                                public void onMessagesRestored(List<Message> messages) {
                                    for(Message message : messages)
                                    {
                                        LiveData<Boolean>exists = dao.isMessageExists(message.getMessageID());
                                        Observer<Boolean>existObserver = new Observer<Boolean>() {
                                            @Override
                                            public void onChanged(Boolean aBoolean) {
                                                if (aBoolean)
                                                {
                                                    dao.updateMessage(message);
                                                }
                                                else
                                                {
                                                    dao.insertNewMessage(message);
                                                }
                                                exists.removeObserver(this);
                                            }
                                        };
                                        exists.observeForever(existObserver);
                                    }
                                }

                                @Override
                                public void onConversationsRestored(List<Conversation> conversations) {
//                                    for (Conversation conversation : conversations)
//                                    {
//                                        if (dao.isConversationExists(conversation.getConversationID()))
//                                        {
//                                            dao.updateConversation(conversation);
//                                        }
//                                        else
//                                        {
//                                            dao.insertNewConversation(conversation);
//                                        }
//                                    }
                                }
                            });
                            server.restoreConversations(currentUser);
                            server.restoreMessages(currentUser);
                            return false;
                        }
                    });
                    break;
                default:
                    Log.e("SETTINGS ERROR","error opening settings");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == CALL_PHONE) {
            phoneCall.setChecked(true);
        }
        else
            phoneCall.setChecked(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
