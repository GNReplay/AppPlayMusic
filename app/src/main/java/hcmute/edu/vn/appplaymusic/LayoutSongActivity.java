package hcmute.edu.vn.appplaymusic;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import hcmute.edu.vn.appplaymusic.Model.UploadFile;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LayoutSongActivity extends AppCompatActivity {
    private ArrayList<UploadFile> mSong;
    private int posotionSong = 0;
    private UploadFile song;
    private ImageView imgSong;
    private TextView tv_songtitle, tv_songsingle, curTime, tolTime;
    private boolean isPlaying;
    private MaterialButton playorpause, btnBack, btnNext, back_activity;
    private SeekBar seekBar;
    private int Duration;
    private int Progress;
    private int Current;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            song = (UploadFile) bundle.get("object_song");
            isPlaying = bundle.getBoolean("status_player");
            int actionMusic = bundle.getInt("action_music");
            Duration = bundle.getInt("duration");
            Current = bundle.getInt("current",0);
            handleLayoutMusic(actionMusic);
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_song);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("send_data_to_activity"));
        tv_songtitle = findViewById(R.id.song_title);
        tv_songsingle = findViewById(R.id.song_single);
        imgSong = findViewById(R.id.img_song);
        playorpause = findViewById(R.id.play_or_pause);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);
        seekBar = findViewById(R.id.slider);
        curTime = findViewById(R.id.curTime);
        tolTime = findViewById(R.id.tolTime);
        back_activity = findViewById(R.id.back_activity);
        back_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mSong = (ArrayList) bundle.getParcelableArrayList("song");
        posotionSong = bundle.getInt("position", 0);
        Duration = bundle.getInt("duration");
        song = mSong.get(posotionSong);
        ShowInforSong();
        isPlaying = true;
        SeekBarDuration();
        sendActionToService(MyService.ACTION_RESUME);
        setStatusButtonPlayorPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void handleLayoutMusic(int action) {
        switch (action) {
            case MyService.ACTION_START:
                ShowInforSong();
                setStatusButtonPlayorPause();
                break;
            case MyService.ACTION_PAUSE:
                setStatusButtonPlayorPause();
                break;
            case MyService.ACTION_RESUME:
                setStatusButtonPlayorPause();
                break;
            case MyService.ACTION_BACK:
               BackMusic();
                break;
            case MyService.ACTION_NEXT:
               NextMusic();
                break;
            case MyService.ACTION_DURATION:
                SeekBarDuration();
                break;
        }
    }

    private void SeekBarDuration(){
        if(Current == 0) {
            seekBar.setMax(Duration);
            tolTime.setText(createTimeLabel(Duration));
        }
        else{
            seekBar.setProgress(Current);
            curTime.setText(createTimeLabel(Current));
        }
    }

    public String createTimeLabel(int duration) {
        String timeLabel = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        timeLabel += min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    private void BackMusic(){
        UploadFile uploadFile;
        if(posotionSong == 0){
            uploadFile = mSong.get(mSong.size() - 1);
            posotionSong = mSong.size() - 1;
        }
        else {
            uploadFile = mSong.get(posotionSong - 1);
            --posotionSong;
        }
        sendSongToService(uploadFile);
    }

    private void NextMusic(){
        UploadFile uploadFile;
        if(posotionSong == (mSong.size() - 1)){
            uploadFile = mSong.get(0);
            posotionSong = 0;
        }
        else {
            uploadFile = mSong.get(posotionSong + 1);
            ++posotionSong;
        }
        sendSongToService(uploadFile);
    }

    private void ShowInforSong() {
        if (song == null) {
            return;
        }
        Picasso.get().load(song.getImageLink()).transform(new CropCircleTransformation()).into(imgSong);
        tv_songtitle.setText(song.getSongTitle());
        tv_songsingle.setText(song.getSinger());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackMusic();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    seekBar.setProgress(progress);
                    Progress = progress;
                    sendActionToService(MyService.ACTION_DURATION);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void setStatusButtonPlayorPause() {
        if(isPlaying){
            playorpause.setBackground(getDrawable(R.drawable.pause));
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);
            imgSong.startAnimation(animation);
        }
        else{
            playorpause.setBackground(getDrawable(R.drawable.play));
            imgSong.clearAnimation();
        }
    }
    private void sendActionToService(int action){
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("action_music_service", action);
        intent.putExtra("progress", Progress);
        startService(intent);
    }

    private void sendSongToService(UploadFile song){
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("song",song);
        startService(intent);
    }
}
