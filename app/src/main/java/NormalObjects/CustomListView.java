package NormalObjects;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;

import android.widget.ListView;

import androidx.core.content.res.ResourcesCompat;

import com.example.woofmeow.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;




public class CustomListView extends ListView {

//    private BaseAdapter adapter;




    private HashMap<Integer,View>selectedHash;

    private onClickOnBody onClickOnBodyListener;
    public void setOnClickOnBodyListener(onClickOnBody listener)
    {
        onClickOnBodyListener = listener;
    }

    public CustomListView(Context context) {
        super(context);
        selectedHash = new HashMap<>();
    }

    public CustomListView(Context context, AttributeSet attrs)

    {
        super(context, attrs);
        selectedHash = new HashMap<>();
    }
    public CustomListView(Context context, AttributeSet attrs,int defStyle)
    {
        super(context, attrs, defStyle);
        selectedHash = new HashMap<>();
    }


    //selects an item from a listView and changes the background of the item to a desirable one
    public void SelectedItem(int position,View selectedView, Drawable drawableResource)
    {
        selectedHash.put(position,selectedView);
      //  adapter = (BaseAdapter) getAdapter();
        MarkAsSelected(selectedView,drawableResource);
    }

    public HashMap<Integer,View> getSelectedItems()
    {
        return selectedHash;
    }



    public View getSelectedView(int position)
    {
        if(selectedHash.containsKey(position))
            return selectedHash.get(position);
        else return null;
    }

    public boolean isSelected(int position)
    {
        return selectedHash.containsKey(position);
    }

    public boolean anySelected()
    {
        return !selectedHash.isEmpty();
    }

    public boolean multipleSelected()
    {
        return selectedHash.size() > 1;
    }
    private void MarkAsSelected(View selectedView,Drawable drawableResource)
    {
        selectedView.setBackground(drawableResource);
    }

    public void unSelectItem(int position,View viewToUnSelect)
    {
        viewToUnSelect.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.conversation_cell_not_selected,getContext().getTheme()));
        selectedHash.remove(position);
    }

    public void unSelectItem(int position)
    {
        if(selectedHash.containsKey(position)) {
            View viewToUnSelect = selectedHash.get(position);
            if(viewToUnSelect!=null)
            {
                viewToUnSelect.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.conversation_cell_not_selected,getContext().getTheme()));
                selectedHash.remove(position);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void unSelectAll()
    {
        Set<Integer>keys = selectedHash.keySet();
        Iterator<Integer>iterator = keys.iterator();
        while (iterator.hasNext())
        {
            selectedHash.get(iterator.next()).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.conversation_cell_not_selected,getContext().getTheme()));
            iterator.remove();
        }
       /* for(int key:keys)
        {
            selectedHash.get(key).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.conversation_cell_not_selected,getContext().getTheme()));
            selectedHash.remove(key);
        }*/
    }



    private void setOnClickListEmptyBody()
    {
        if(onClickOnBodyListener!=null)
            onClickOnBodyListener.onViewEmptyBodyClick();
    }
}
