package com.example.woofmeow;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import Fragments.PreferenceFragment;


public class PreferenceActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content,new PreferenceFragment()).commit();

    }



    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        String key = pref.getKey();
        Bundle bundle = pref.getExtras();
        bundle.putString("key",key);
        Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(),pref.getFragment());
        fragment.setArguments(bundle);
        fragment.setTargetFragment(caller,0);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,fragment).addToBackStack(null).commit();
        return true;
    }
}
