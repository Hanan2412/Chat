package Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.woofmeow.R;
import com.google.android.material.tabs.TabLayout;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;


public class GeneralFragment extends Fragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.general_fragment_layout,container,false);
        TabLayout tabLayout = view.findViewById(R.id.tabs_layout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        PagerAdapter adapter = new PagerAdapter(requireActivity().getSupportFragmentManager(),BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);





        return view;
    }




    private class PagerAdapter extends FragmentStatePagerAdapter
    {

        public PagerAdapter(@NonNull FragmentManager fm,int behavior) {
            super(fm,behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return TimePickerFragment.getInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "Meeting start";
            else if (position == 1)
                return "Meeting end";
            else return "Error";
        }
    }
}
