package hcmute.edu.vn.appplaymusic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Song> songList;
    public ItemAdapter(List<Song> songList){
        this.songList = songList;
    }
    public void setFilteredList(List<Song> filteredList){
        this.songList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.itemTv_title_song.setText(song.getTitle());
        holder.itemTv_singer_song.setText(song.getSinger());
        holder.itemIv_img_song.setImageResource(song.getImage());
    }

    @Override
    public int getItemCount() {
        if (songList == null){
            return 0;
        }
        else{
            return songList.size();
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        private ImageView itemIv_img_song;
        private TextView itemTv_title_song;
        private TextView itemTv_singer_song;
        private CardView cardView;


        public ItemViewHolder(View itemView){
            super(itemView);

            cardView = itemView.findViewById(R.id.eachCardView);
            itemTv_title_song = itemView.findViewById(R.id.tv_title_song);
            itemTv_singer_song = itemView.findViewById(R.id.tv_singer_song);
            itemIv_img_song = itemView.findViewById(R.id.img_song);
        }
    }
}
