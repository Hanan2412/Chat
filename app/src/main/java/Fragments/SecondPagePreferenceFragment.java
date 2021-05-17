package Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.example.woofmeow.PreferencesUpdate;
import com.example.woofmeow.R;
import com.google.firebase.auth.FirebaseAuth;

import Controller.CController;

@SuppressWarnings({"Convert2Lambda", "ConstantConditions"})
public class SecondPagePreferenceFragment extends PreferenceFragmentCompat implements PreferencesUpdate {

    private final String CHAT_PREFERENCE = "ChatPreference";
    private final String ABOUT_PREFERENCE = "AboutPreference";
    private final String UPDATE_LOG = "updateLog";
    private final String ACCOUNT_PREFERENCES = "AccountPreference";
    private CController controller;
    private final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @SuppressLint("RestrictedApi")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        controller = CController.getController();
        controller.setPreferencesInterface(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String key = bundle.getString("key", "noKey");
            switch (key) {
                case CHAT_PREFERENCE:
                    setPreferencesFromResource(R.xml.chat_preference, rootKey);
                    SwitchPreferenceCompat privateNotificationsSwitch = findPreference("privateNotifications");
                    SwitchPreferenceCompat chatNotificationsSwitch = findPreference("chatNotifications");
                    SwitchPreferenceCompat newChatNotificationsSwitch = findPreference("newChatNotifications");
                    SwitchPreferenceCompat allNotificationsSwitch = findPreference("all_notifications");
                    SwitchPreferenceCompat showNotificationLikeThis = findPreference("iconNotification");
                    ListPreference actionBtnDefault = findPreference("ActionButton");
                    SwitchPreferenceCompat longPressSwitch = findPreference("Long press");
                    CheckBoxPreference sendButtonLP = findPreference("SendButtonLP");
                    CheckBoxPreference actionButtonLP = findPreference("ActionButtonLP");
                    ListPreference loadMessages = findPreference("messagesLoad");






                    loadMessages.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            System.out.println("loadMessages changed");
                            return false;
                        }
                    });


                    allNotificationsSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {

                            if (!allNotificationsSwitch.isChecked()) {
                                privateNotificationsSwitch.setChecked(false);
                                chatNotificationsSwitch.setChecked(false);
                                newChatNotificationsSwitch.setChecked(false);
                            } else {
                                privateNotificationsSwitch.setChecked(true);
                                chatNotificationsSwitch.setChecked(true);
                                newChatNotificationsSwitch.setChecked(true);
                            }
                            return false;
                        }
                    });

                privateNotificationsSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (!privateNotificationsSwitch.isChecked())
                            allNotificationsSwitch.setChecked(false);
                        else if(chatNotificationsSwitch.isChecked() && newChatNotificationsSwitch.isChecked())
                            allNotificationsSwitch.setChecked(true);
                        return false;
                    }
                });

                longPressSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (longPressSwitch.isChecked()) {
                            {
                                sendButtonLP.setVisible(true);
                                actionButtonLP.setVisible(true);
                                sendButtonLP.setChecked(true);
                                actionButtonLP.setChecked(true);
                            }
                        } else {
                            sendButtonLP.setVisible(false);
                            actionButtonLP.setVisible(false);
                            sendButtonLP.setChecked(false);
                            actionButtonLP.setChecked(false);
                        }
                        return false;
                    }
                });

                sendButtonLP.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (!sendButtonLP.isChecked()) {
                            if (!actionButtonLP.isChecked()) {
                                longPressSwitch.performClick();
                            }
                        }
                        return false;
                    }
                });
                actionButtonLP.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (!actionButtonLP.isChecked()) {
                            if (!sendButtonLP.isChecked())
                                longPressSwitch.performClick();
                        }
                        return false;
                    }
                });

                chatNotificationsSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if(!chatNotificationsSwitch.isChecked())
                            allNotificationsSwitch.setChecked(false);
                        else if(newChatNotificationsSwitch.isChecked() && privateNotificationsSwitch.isChecked())
                            allNotificationsSwitch.setChecked(true);
                        return false;
                    }
                });

                newChatNotificationsSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if(!newChatNotificationsSwitch.isChecked())
                            allNotificationsSwitch.setChecked(false);
                        else if(chatNotificationsSwitch.isChecked() && privateNotificationsSwitch.isChecked())
                            allNotificationsSwitch.setChecked(true);
                        return false;
                    }
                });

                actionBtnDefault.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // callback.onChatPreferenceChange(actionBtnLP,true,Integer.parseInt(actionBtnDefault.getValue()));
                        return false;
                    }
                });
                break;

                case ABOUT_PREFERENCE:
                    setPreferencesFromResource(R.xml.about_preference, rootKey);
                    break;
                case UPDATE_LOG:
                    setPreferencesFromResource(R.xml.update_log,rootKey);
                    break;
                case ACCOUNT_PREFERENCES:
                {
                    setPreferencesFromResource(R.xml.account_preference,rootKey);
                    SwitchPreferenceCompat saveData = findPreference("dataSave");
                    CheckBoxPreference saveLocally = findPreference("LocalSave");
                    CheckBoxPreference cloudSave = findPreference("CloudSave");
                    saveData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if(saveData.isChecked()) {
                                saveLocally.setVisible(true);
                                cloudSave.setVisible(true);
                            }
                            else
                            {
                                cloudSave.setChecked(false);
                                saveLocally.setVisible(false);
                                cloudSave.setVisible(false);
                            }
                            return false;
                        }
                    });

                    Preference deleteAllData = findPreference("deleteAllConversations");
                    deleteAllData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setCancelable(true)
                                    .setPositiveButton("Delete my conversations", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            controller.onPreferenceDelete("users/" + currentUser + "/conversations");
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
                                            controller.onPreferenceDelete("users/" + currentUser);
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
                    SwitchPreferenceCompat allowOthersToFindMe = findPreference("findMe");
                    allowOthersToFindMe.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if(allowOthersToFindMe.isChecked())
                                 controller.onPreferenceChange("users/" + currentUser + "canBeFound",true);
                            else
                                controller.onPreferenceChange("users/" + currentUser + "canBeFound",false);
                            return false;
                        }
                    });
                    break;
                }
                default:
                    Toast.makeText(requireContext(), "Error opening settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean darkMode = preferences.getBoolean("darkView", false);
        if (darkMode)
        {
            requireContext().setTheme(R.style.preferenceStyleDark);
            view.setBackgroundColor(getResources().getColor(R.color.black, requireContext().getTheme()));
        }
        else
        {
            requireContext().setTheme(R.style.preferenceStyleLight);
            view.setBackgroundColor(getResources().getColor(android.R.color.white, requireContext().getTheme()));
        }
        return view;
    }


}
