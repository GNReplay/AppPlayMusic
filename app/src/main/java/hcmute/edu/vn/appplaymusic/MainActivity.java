package hcmute.edu.vn.appplaymusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.appplaymusic.Model.UploadFile;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<UploadFile> songList;
    private ItemAdapter itemAdapter;
    private SearchView searchView;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private RelativeLayout relativeLayout;
    private ImageView imgSong, playorpause, imgClear, imgBack, imgNext;
    private TextView tvTitleSong, tvSingleSong;
    private UploadFile msong;
    private Boolean isPlaying;
    private int positionSong = 0;
    private int Duration;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            msong = (UploadFile) bundle.get("object_song");
            isPlaying = bundle.getBoolean("status_player");
            int actionMusic = bundle.getInt("action_music");
            Duration = bundle.getInt("duration");
            handleLayoutMusic(actionMusic);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("send_data_to_activity"));
        relativeLayout = findViewById(R.id.layout_playorpause);
        imgSong = findViewById(R.id.img_song);
        playorpause = findViewById(R.id.img_play_or_pause);
        imgClear = findViewById(R.id.img_close);
        tvTitleSong = findViewById(R.id.tv_title_song);
        tvSingleSong = findViewById(R.id.tv_singer_song);
        imgBack = findViewById(R.id.img_back);
        imgNext = findViewById(R.id.img_next);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        songList = new ArrayList<>();

        itemAdapter = new ItemAdapter(MainActivity.this,songList);
        recyclerView.setAdapter(itemAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("songs");

        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songList.clear();
                for(DataSnapshot dss:snapshot.getChildren()){
                    UploadFile uploadFile = dss.getValue(UploadFile.class);
                    uploadFile.setMkey(dss.getKey());
                    songList.add(uploadFile);
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<UploadFile> ArraySong = new ArrayList<UploadFile>(songList);
                Intent intent = new Intent(MainActivity.this,LayoutSongActivity.class);
                intent.putExtra("song", ArraySong)
                                .putExtra("position",positionSong)
                        .putExtra("duration",Duration);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public void playSong(List<UploadFile> songList, int adapterPosition) throws IOException {
        UploadFile uploadFile = songList.get(adapterPosition);
        Intent intent = new Intent(this,MyService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_song",uploadFile);
        intent.putExtras(bundle);
        positionSong = adapterPosition;
        startService(intent);
    }

    private void BackMusic(){
        UploadFile uploadFile;
        if(positionSong == 0){
            uploadFile = songList.get(songList.size() - 1);
            positionSong = songList.size() - 1;
        }
        else {
            uploadFile = songList.get(positionSong - 1);
            --positionSong;
        }
        sendSongToService(uploadFile);
    }

    private void NextMusic(){
        UploadFile uploadFile;
        if(positionSong == (songList.size() - 1)){
            uploadFile = songList.get(0);
            positionSong = 0;
        }
        else {
            uploadFile = songList.get(positionSong + 1);
            ++positionSong;
        }
        sendSongToService(uploadFile);
    }

    private void handleLayoutMusic(int action) {
        switch (action) {
            case MyService.ACTION_START:
                relativeLayout.setVisibility(View.VISIBLE);
                ShowInforSong();
                setStatusButtonPlayorPause();
                break;
            case MyService.ACTION_PAUSE:
                setStatusButtonPlayorPause();
                break;
            case MyService.ACTION_RESUME:
                setStatusButtonPlayorPause();
                break;
            case MyService.ACTION_CLEAR:
                relativeLayout.setVisibility(View.GONE);
                break;
            case MyService.ACTION_BACK:
                BackMusic();
                break;
            case MyService.ACTION_NEXT:
                NextMusic();
                break;
        }
    }

    private void ShowInforSong() {
        if (msong == null) {
            return;
        }
        Picasso.get().load(msong.getImageLink()).into(imgSong);
        tvTitleSong.setText(msong.getSongTitle());
        tvSingleSong.setText(msong.getSinger());

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackMusic();
            }
        });

        imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NextMusic();
            }
        });

        playorpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    sendActionToService(MyService.ACTION_PAUSE);
                }
                else{
                    sendActionToService(MyService.ACTION_RESUME);
                }
            }
        });

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendActionToService(MyService.ACTION_CLEAR);
            }
        });
    }

    private void setStatusButtonPlayorPause() {
        if(isPlaying){
            playorpause.setImageResource(R.drawable.pause);
        }
        else{
            playorpause.setImageResource(R.drawable.play);
        }
    }

    private void sendActionToService(int action){
        ArrayList<UploadFile> ArraySong = new ArrayList<UploadFile>(songList);
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("action_music_service", action);
        startService(intent);
    }

    private void sendSongToService(UploadFile song){
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("song",song);
        startService(intent);
    }
}