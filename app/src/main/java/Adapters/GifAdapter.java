package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.woofmeow.R;

import java.util.ArrayList;
import java.util.List;

import NormalObjects.Gif;

public class GifAdapter extends BaseAdapter {


    private List<Gif> gifs;

    public GifAdapter() {
        gifs = new ArrayList<>();

    }

    public List<Gif> getGif2s() {
        return gifs;
    }

    @Override
    public int getCount() {
        return gifs.size();
    }

    @Override
    public Object getItem(int position) {
        return gifs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater != null) {

            convertView = layoutInflater.inflate(R.layout.image_cell, parent, false);
            ImageView gif = convertView.findViewById(R.id.gif);
            Glide.with(parent.getContext()).load(gifs.get(position).getUrl()).placeholder(R.drawable.ic_baseline_gif_box_24).into(gif);
        }
        return convertView;
    }

    public void addGif(Gif gif) {
        gifs.add(gif);
        notifyDataSetChanged();
    }

    public void clear()
    {
        gifs.clear();
        notifyDataSetChanged();
    }
}
