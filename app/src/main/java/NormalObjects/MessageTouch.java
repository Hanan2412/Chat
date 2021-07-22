package NormalObjects;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


public class MessageTouch extends ItemTouchHelper.SimpleCallback{

    private TouchListener listener;

    public MessageTouch(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
    }

    @Override
    public boolean onMove(@NonNull  RecyclerView recyclerView, @NonNull  RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return listener.onMove(recyclerView, viewHolder, target);
    }

    @Override
    public void onSwiped(@NonNull  RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwipe(viewHolder, direction);
    }

    @Override
    public void onChildDraw(@NonNull  Canvas c, @NonNull  RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
        {
            View itemView = viewHolder.itemView;
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setTextSize(50);
            int h = itemView.getHeight();
            if(dX>0)
            {
                c.drawText("quote",(float) (itemView.getLeft() + (itemView.getRight()/12)),(float) (itemView.getBottom()-(h/2)+18),paint);
            }
            else if(dX< 0)
            {
                c.drawText("quote",(float) (itemView.getRight() - itemView.getRight()/6),(float) (itemView.getBottom()-(h/2)+18),paint);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public void setListener(TouchListener listener)
    {
        this.listener = listener;
    }
}
