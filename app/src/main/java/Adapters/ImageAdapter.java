package Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.woofmeow.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"Convert2Lambda"})
public class ImageAdapter extends BaseAdapter {

    private List<Bitmap> bitmapList;
    private int selected = -1;
    private Uri galleryImage;
    public ImageAdapter() {
        bitmapList = new ArrayList<>();
        Bitmap.Config config = Bitmap.Config.ALPHA_8;
        for (int i = 0; i < 6;i++){
             bitmapList.add(Bitmap.createBitmap(10,10,config));
        }

    }


    @Override
    public int getCount() {
        return bitmapList.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return bitmapList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater != null) {
            convertView = layoutInflater.inflate(R.layout.grid_cell, parent, false);
            ImageView backgroundImage = convertView.findViewById(R.id.image);
            if (position == bitmapList.size())
            {
                if (galleryImage!=null)
                {
                    try {
                        InputStream stream = parent.getContext().getContentResolver().openInputStream(galleryImage);
                        Drawable drawable = Drawable.createFromStream(stream, galleryImage.toString());
                        backgroundImage.setImageDrawable(drawable);
                        Drawable drawable1 = AppCompatResources.getDrawable(parent.getContext(), R.drawable.ic_baseline_add_circle_24);
                        backgroundImage.setForeground(drawable1);
                        backgroundImage.setForegroundGravity(Gravity.CENTER);
                        backgroundImage.setScaleType(ImageView.ScaleType.CENTER);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else
                    backgroundImage.setImageResource(R.drawable.ic_baseline_add_circle_24);
            }
            else{
                backgroundImage.setImageBitmap(bitmapList.get(position));
                backgroundImage.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            if (position == selected)
                convertView.setSelected(true);
        }
        return convertView;

    }

    public void setBackgroundsList(List<Bitmap>bitmapList)
    {
        this.bitmapList = bitmapList;
        notifyDataSetChanged();
    }

    public void setSelected(int selected)
    {
        this.selected = selected;
    }

    public void setGalleryImage(Uri uri)
    {
        galleryImage = uri;
        notifyDataSetChanged();
    }
}
