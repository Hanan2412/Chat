package NormalObjects;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.woofmeow.R;

import Adapters.ConversationsAdapter2;

public class ConversationTouch extends ItemTouchHelper.SimpleCallback {

    private ConversationsAdapter2 adapter2;

    private TouchListener listener;
    public ConversationTouch(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
    }

    @Override
    public boolean onMove(@NonNull  RecyclerView recyclerView, @NonNull  RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return listener.onMove(recyclerView, viewHolder, target);
    }

    @Override
    public void onSwiped(@NonNull  RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwipe(viewHolder,direction);
    }

    @Override
    public void onChildDraw(@NonNull  Canvas c, @NonNull  RecyclerView recyclerView, @NonNull  RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
        {
            boolean action;
            View itemView = viewHolder.itemView;
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(50);
            int h = itemView.getHeight();

            if(dX>0)
            {
                paint.setColor(Color.GREEN);
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                        (float) itemView.getBottom(), paint);
                paint.setColor(Color.BLUE);
                action =  !adapter2.getConversation(viewHolder.getAdapterPosition()).isMuted();
                if(action)
                     c.drawText(recyclerView.getContext().getResources().getString(R.string.mute),(float) (itemView.getLeft() + (itemView.getRight()/12)),(float) (itemView.getBottom()-(h/2)+18),paint);
                else
                    c.drawText(recyclerView.getContext().getResources().getString(R.string.unmute),(float) (itemView.getLeft() + (itemView.getRight()/12)),(float) (itemView.getBottom()-(h/2)+18),paint);
            }
            else{
                paint.setColor(Color.RED);
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                paint.setColor(Color.BLUE);
                action =  !adapter2.getConversation(viewHolder.getAdapterPosition()).isBlocked();
                if(action)
                     c.drawText("Delete",(float) (itemView.getRight() - itemView.getRight()/6),(float) (itemView.getBottom()-(h/2)+18),paint);
                else
                    c.drawText(recyclerView.getContext().getResources().getString(R.string.unblock),(float) (itemView.getRight() - itemView.getRight()/6),(float) (itemView.getBottom()-(h/2)+18),paint);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public void setListener(@NonNull TouchListener listener)
    {
        this.listener = listener;
    }

    public void setConversations(ConversationsAdapter2 adapter2)
    {
       this.adapter2 = adapter2;
    }
}
