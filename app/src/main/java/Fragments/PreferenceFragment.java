package Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.PeriodicSync;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.R;

import Consts.PermissionType;


@SuppressWarnings({"ConstantConditions", "Convert2Lambda"})
public class PreferenceFragment extends PreferenceFragmentCompat {
    private ActivityResultLauncher<String> backgroundImage;
    private ActivityResultLauncher<Intent> backgroundImage2;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        DropDownPreference viewModePreference = findPreference("darkMode");
        viewModePreference.setSummary(viewModePreference.getValue());
        viewModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                DropDownPreference dropDownPreference = (DropDownPreference) preference;
                dropDownPreference.setSummary(newValue.toString());
                switch (newValue.toString())
                {
                    case "system default":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                    case "light":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case "dark":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    default:
                        Log.e("PreferenceFragment","Look error - non existing value selected");
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                return true;
            }
        });
//        backgroundImage = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
//            @Override
//            public void onActivityResult(Uri result) {
//                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("background", Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString("backgroundImage",result.toString());
//                editor.apply();
//            }
//        });
//        backgroundImage2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//                if (result.getResultCode() == RESULT_OK) {
//                    Uri uri = result.getData().getData();
//                    if (uri!=null) {
//                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("background", Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString("backgroundImage", result.getData().getData().toString());
//                        editor.apply();
//                    }
//                }
//            }
//        });
//        Preference backgroundPreference = findPreference("background");
//        backgroundPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                        .setType("image/*")
//                        .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////                backgroundImage.launch("image/*");
//                backgroundImage2.launch(intent);
//                return true;
//            }
//        });
    }
}
