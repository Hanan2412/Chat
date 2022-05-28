package Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.R;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Convert2Lambda")
public class GifSuggestionsAdapter extends RecyclerView.Adapter<GifSuggestionsAdapter.SuggestionsViewHolder> {

    private List<String>suggestions;
    public GifSuggestionsAdapter()
    {
        suggestions = new ArrayList<>();
    }
    public interface onItemClickListener{
        void onItemClick(String text);
    }
    private onItemClickListener listener;
    public void setListener(onItemClickListener listener)
    {
        this.listener = listener;
    }
    public void addSuggestion(String suggestion)
    {
        suggestions.add(suggestion);
        notifyItemInserted(getItemCount()-1);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear()
    {
        suggestions.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GifSuggestionsAdapter.SuggestionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gif_suggestion,parent,false);
        return new SuggestionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GifSuggestionsAdapter.SuggestionsViewHolder holder, int position) {
            String suggestion = suggestions.get(position);
            holder.suggestion.setText(suggestion);
            holder.suggestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!=null)
                        listener.onItemClick(suggestion);
                }
            });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class SuggestionsViewHolder extends RecyclerView.ViewHolder
    {
        TextView suggestion;
        public SuggestionsViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestion = itemView.findViewById(R.id.suggestion);
        }
    }
}
