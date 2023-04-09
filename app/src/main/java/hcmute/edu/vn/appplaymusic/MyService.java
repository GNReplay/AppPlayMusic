package hcmute.edu.vn.appplaymusic;

import static hcmute.edu.vn.appplaymusic.MyApplication.CHANEL_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import hcmute.edu.vn.appplaymusic.Model.UploadFile;

public class MyService extends Service {
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME= 2;
    public static final int ACTION_CLEAR = 3;
    public static final int ACTION_START = 4;
    public static final int ACTION_BACK = 5;
    public static final int ACTION_NEXT = 6;
    public static final int ACTION_DURATION = 7;
    private MediaPlayer mediaPlayer;
    private Boolean isPlaying = null;
    private UploadFile mSong;
    private int Duration;
    private int Progress;
    private int Current_position = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Message","My Service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            UploadFile uploadFile = (UploadFile) bundle.get("object_song");
            if(uploadFile != null) {
                mSong = uploadFile;
                try {
                    startMusic(uploadFile);
                    sendNotification(uploadFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        int actionMusic = intent.getIntExtra("action_music_service",0);
        Progress = intent.getIntExtra("progress",0);
        UploadFile song = (UploadFile) intent.getSerializableExtra("song");
        if(song != null) {
            mSong = song;
            try {
                startMusic(song);
                sendNotification(song);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            handleActionMusic(actionMusic);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return START_NOT_STICKY;
    }

    private void startMusic(UploadFile uploadFile) throws IOException {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(uploadFile.getSongLink());
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Duration = mediaPlayer.getDuration();
                sendActiveActivity(ACTION_DURATION);
                mp.start();
            }
        });
      mediaPlayer.prepareAsync();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    try {
                        if (isPlaying) {
                            Message msg = new Message();
                            msg.what = mediaPlayer.getCurrentPosition();
                            handler.sendMessage(msg);
                            Thread.sleep(900);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                sendActiveActivity(ACTION_NEXT);
            }
        });
        isPlaying = true;
        sendActiveActivity(ACTION_START);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int current_position = msg.what;
            Current_position = current_position;
            sendActiveActivity(ACTION_DURATION);
        }
    };

    private void handleActionMusic(int action) throws IOException {
        switch (action){
            case ACTION_PAUSE:
                PauseMusic();
                break;
            case ACTION_RESUME:
                ResumeMusic();
                break;
            case ACTION_CLEAR:
                stopSelf();
                sendActiveActivity(ACTION_CLEAR);
                break;
            case ACTION_BACK:
                sendActiveActivity(ACTION_BACK);
                break;
            case ACTION_NEXT:
                sendActiveActivity(ACTION_NEXT);
                break;
            case ACTION_DURATION:
                Seekbar();
                break;
        }
    }

    private void PauseMusic() {
        if(mediaPlayer != null && isPlaying){
            mediaPlayer.pause();
            isPlaying = false;
            sendNotification(mSong);
            sendActiveActivity(ACTION_PAUSE);
        }
    }

    private void ResumeMusic() {
        if(mediaPlayer != null && !isPlaying){
            mediaPlayer.start();
            isPlaying = true;
            sendNotification(mSong);
            sendActiveActivity(ACTION_RESUME);
        }
    }

    private void Seekbar(){
        mediaPlayer.seekTo(Progress);
    }

//    private void sendNotification(@NotNull UploadFile song) {
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_custom_notification);
//        remoteViews.setTextViewText(R.id.tv_title_song, song.getSongTitle());
//        remoteViews.setTextViewText(R.id.tv_single_song, song.getSinger());
//        remoteViews.setImageViewResource(R.id.play_or_pause, R.drawable.pause);
//
//        if (isPlaying) {
//            remoteViews.setOnClickPendingIntent(R.id.play_or_pause, getPendingIntent(this, ACTION_PAUSE));
//            remoteViews.setImageViewResource(R.id.play_or_pause, R.drawable.pause);
//        } else {
//            remoteViews.setOnClickPendingIntent(R.id.play_or_pause, getPendingIntent(this, ACTION_RESUME));
//            remoteViews.setImageViewResource(R.id.play_or_pause, R.drawable.play);
//        }
//
//        remoteViews.setOnClickPendingIntent(R.id.close, getPendingIntent(this, ACTION_CLEAR));
//        Notification notification = new NotificationCompat.Builder(this, CHANEL_ID)
//                .setSmallIcon(R.drawable.ic_notification)
//                .setContentIntent(pendingIntent)
//                .setCustomContentView(remoteViews)
//                .setSound(null)
//                .build();
//        Picasso.get().load(song.getImageLink()).into(remoteViews, R.id.img_song,1, notification);
//        startForeground(1, notification);
//    }


    private void sendNotification(@NotNull UploadFile song){
        new Thread(() -> {
            try {
                MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this,"tag");
                Bitmap bitmap = Picasso.get().load(song.getImageLink()).get();
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANEL_ID)
                .setSmallIcon(R.drawable.small_music)
                .setSubText("AppPlayMusic")
                .setContentTitle(song.getSongTitle())
                .setContentText(song.getSinger())
                .setLargeIcon(bitmap)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2,3)
                        .setMediaSession(mediaSessionCompat.getSessionToken()));

                if(isPlaying){
                    notificationBuilder.addAction(R.drawable.previous,"Previous",getPendingIntent(this,ACTION_BACK))
                            .addAction(R.drawable.pause,"Pause",getPendingIntent(this,ACTION_PAUSE))
                            .addAction(R.drawable.next,"Next",getPendingIntent(this,ACTION_NEXT))
                            .addAction(R.drawable.close,"Close",getPendingIntent(this,ACTION_CLEAR));
                }
                else{
                    notificationBuilder.addAction(R.drawable.previous,"Previous",getPendingIntent(this,ACTION_BACK))
                            .addAction(R.drawable.play,"Pause",getPendingIntent(this,ACTION_RESUME))
                            .addAction(R.drawable.next,"Next",getPendingIntent(this,ACTION_NEXT))
                            .addAction(R.drawable.close,"Close",getPendingIntent(this,ACTION_CLEAR));
                }
                Notification notification = notificationBuilder.build();
            startForeground(1, notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private PendingIntent getPendingIntent(Context context, int action){
        Intent intent = new Intent(this, MyReceiver.class);
        intent.putExtra("action_music",action);
        return PendingIntent.getBroadcast(context.getApplicationContext(),action,intent,PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e("Message","My Service onDestroy");
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void sendActiveActivity(int action){
        Intent intent = new Intent("send_data_to_activity");
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_song", mSong);
        bundle.putBoolean("status_player",isPlaying);
        bundle.putInt("action_music",action);
        bundle.putInt("duration",Duration);
        bundle.putInt("current",Current_position);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}