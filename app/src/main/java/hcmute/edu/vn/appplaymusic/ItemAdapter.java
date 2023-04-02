package hcmute.edu.vn.appplaymusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import hcmute.edu.vn.appplaymusic.Model.UploadFile;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    Context context;
    List<UploadFile> songList;
    public ItemAdapter(Context context,List<UploadFile> songList){
        this.context = context;
        this.songList = songList;
    }

    @NonNull
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        UploadFile song = songList.get(position);
        holder.itemTv_title_song.setText(song.getSongTitle());
        holder.itemTv_singer_song.setText(song.getSinger());
        Picasso.get().load(song.getImageLink()).into(holder.itemIv_img_song);
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

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                ((MainActivity)context).playSong(songList,getAdapterPosition());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
