package Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.woofmeow.R;


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
    }
}
