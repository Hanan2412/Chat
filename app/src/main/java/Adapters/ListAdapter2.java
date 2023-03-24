package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.woofmeow.R;

import java.util.ArrayList;
import java.util.List;


public class ListAdapter2 extends BaseAdapter {

    private int itemsColor = -1;
    private int titlesColor = -1;

    private List<String>items;
    private List<String>titles;

    public ListAdapter2()
    {
        items = new ArrayList<>();
    }

    public void setItems(List<String>items)
    {
        this.items = items;
        notifyDataSetChanged();
    }

    public void addItem(String item)
    {
        this.items.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(int index)
    {
        this.items.remove(index);
        notifyDataSetChanged();
    }

    public void setTitles(List<String>titles)
    {
        this.titles = titles;
        notifyDataSetChanged();
    }

    public void addTitle(String title)
    {
        this.titles.add(title);
        notifyDataSetChanged();
    }

    public void removeTitle(int index)
    {
        this.titles.remove(index);
        notifyDataSetChanged();
    }

    public void updateItem(String txt, int position)
    {
        this.items.set(position,txt);
        notifyDataSetChanged();
    }

    public void setTextColor(int itemsColor, int titlesColor)
    {
        this.titlesColor = titlesColor;
        this.itemsColor = itemsColor;
    }
    public void setItemsColor(int itemsColor)
    {
        this.itemsColor = itemsColor;
    }
    public void setTitlesColor(int titlesColor)
    {
        this.titlesColor = titlesColor;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater)viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater!=null) {
            view = layoutInflater.inflate(R.layout.text_cell, viewGroup, false);
            TextView textView = view.findViewById(R.id.infoText);
            if (itemsColor!=-1)
                textView.setTextColor(itemsColor);
            TextView title = view.findViewById(R.id.title);
            if (titlesColor!=-1)
                title.setTextColor(titlesColor);
            if (items.get(i) != null)
                textView.setText(String.valueOf(items.get(i)));
            title.setText(titles.get(i));
        }
        return view;
    }
}
