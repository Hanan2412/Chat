package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.woofmeow.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class GridImageAdapter extends BaseAdapter {

    private List<String> imagePaths;

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater != null) {
            view = layoutInflater.inflate(R.layout.image_layout, viewGroup, false);
            ImageView image = view.findViewById(R.id.imagePreview);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            String path = imagePaths.get(position);
            Picasso.get().load(new File(path)).into(image);
        }
        return view;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }
}
