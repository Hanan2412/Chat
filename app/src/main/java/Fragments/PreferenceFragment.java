package Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.example.woofmeow.R;

import DataBase.DBActive;


@SuppressWarnings({"ConstantConditions", "Convert2Lambda"})
public class PreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.settings, rootKey);
        SwitchPreferenceCompat darkModeSwitch = findPreference("darkView");
        darkModeSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(darkModeSwitch.isChecked()) {
                    requireContext().setTheme(R.style.preferenceStyleDark);
                    darkModeSwitch.setTitle("Lights on");
                }
                else
                {
                    requireContext().setTheme(R.style.preferenceStyleLight);
                    darkModeSwitch.setTitle("Lights off");
                }
                requireActivity().recreate();
                return true;
            }
        });
        Preference resetDB = findPreference("resetDB");
        resetDB.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DBActive dbActive = DBActive.getInstance(requireContext());
                dbActive.ResetDB();
                Toast.makeText(requireContext(), "Database reset has been done", Toast.LENGTH_SHORT).show();
                return true;
            }
        });


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            boolean darkMode = preferences.getBoolean("darkView", false);
            if (darkMode)
            {
                requireContext().setTheme(R.style.preferenceStyleDark);
                findPreference("darkView").setTitle("Lights on");
                view.setBackgroundColor(getResources().getColor(R.color.black, requireContext().getTheme()));
            }
            else{
                requireContext().setTheme(R.style.preferenceStyleLight);
                findPreference("darkView").setTitle("Lights off");
                view.setBackgroundColor(getResources().getColor(android.R.color.white, requireContext().getTheme()));
            }
        }
        return view;
    }
}
