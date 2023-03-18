package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapterRV extends RecyclerView.Adapter<ImageAdapterRV.ImageAdapterViewHolder> {

    private List<String>imagesPaths;

    public ImageAdapterRV() {
        imagesPaths = new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_cell,parent, false);
        return new ImageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapterViewHolder holder, int position) {
        Picasso.get().load(imagesPaths.get(position)).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return imagesPaths.size();
    }

    public class ImageAdapterViewHolder extends RecyclerView.ViewHolder{

        ImageView profileImage;

        public ImageAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.image);
        }
    }

    public void setImagesPaths(List<String>list)
    {
        this.imagesPaths = list;
        notifyDataSetChanged();
    }

    public void addImagePath(String path)
    {
        if (imagesPaths != null)
        {
            imagesPaths.add(path);
            notifyItemInserted(imagesPaths.size()-1);
        }
    }

    public void removeImage(int index)
    {
        if (imagesPaths != null)
        {
            if (index < imagesPaths.size())
            {
                imagesPaths.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    public void updateImage(int index, String path)
    {
        imagesPaths.add(index, path);
        notifyItemChanged(index);
    }
}
