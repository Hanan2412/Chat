package Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.PreferencesUpdate;
import com.example.woofmeow.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import Controller.CController;

@SuppressWarnings({"Convert2Lambda", "ConstantConditions"})
public class SecondPagePreferenceFragment extends PreferenceFragmentCompat implements PreferencesUpdate {

    private final String CHAT_PREFERENCE = "ChatPreference";
    private final String ACCOUNT_PREFERENCES = "AccountPreference";
    private final String NOTIFICATIONS_PREFERENCES = "NotificationsPreference";
    private final int CALL_PHONE = 7;
    private CController controller;
    private final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private SwitchPreferenceCompat phoneCall;
    private final String preferencePath = "users/" + currentUser + "/preferences/";
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
                }
                case NOTIFICATIONS_PREFERENCES:{
                    setPreferencesFromResource(R.xml.notification_preferences,rootKey);
                    break;
                }
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean darkMode = preferences.getBoolean("darkView", false);
        if (darkMode) {
            requireContext().setTheme(R.style.preferenceStyleDark);
            view.setBackgroundColor(getResources().getColor(R.color.black, requireContext().getTheme()));
        } else {
            requireContext().setTheme(R.style.preferenceStyleLight);
            view.setBackgroundColor(getResources().getColor(android.R.color.white, requireContext().getTheme()));
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.setPreferencesInterface(null);
    }
}
