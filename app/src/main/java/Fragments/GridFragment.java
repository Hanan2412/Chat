package Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.woofmeow.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import Adapters.GridImageAdapter;
import Adapters.ImageAdapter;
import Adapters.ListAdapter2;

@SuppressWarnings("Convert2Lambda")
public class GridFragment extends Fragment {


    private GridImageAdapter imageAdapter;
    private LinearLayout searchLayout;
    private EditText searchText;
    private GridView grid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grid_layout, container, false);
        setHasOptionsMenu(true);
        grid = view.findViewById(R.id.gridView);
        grid.setAdapter(imageAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageFragment imageFragment = new ImageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("uri", (String) imageAdapter.getItem(i));
                imageFragment.setArguments(bundle);
                imageFragment.show(requireActivity().getSupportFragmentManager(),"image fragment");
            }
        });
        searchLayout = view.findViewById(R.id.searchLayout);
        searchText = view.findViewById(R.id.searchText);
        ExtendedFloatingActionButton searchBtn = view.findViewById(R.id.scrollToNext);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }

    public void setImageAdapter(GridImageAdapter imageAdapter) {
        this.imageAdapter = imageAdapter;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search)
        {
            Animation in = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down);
            Animation out = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up);
            if (searchLayout.getVisibility() == View.GONE) {
                searchText.setText("");
                searchLayout.setVisibility(View.VISIBLE);
                searchLayout.startAnimation(in);
                grid.startAnimation(in);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchLayout.setVisibility(View.GONE);
                    }
                }, out.getDuration());
                searchLayout.startAnimation(out);
                grid.startAnimation(out);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
