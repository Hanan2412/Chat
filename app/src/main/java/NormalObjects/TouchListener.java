package NormalObjects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface TouchListener {
    void onSwipe(@NonNull RecyclerView.ViewHolder viewHolder,int swipeDirection);
    boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target);
}
