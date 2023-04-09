package hcmute.edu.vn.appplaymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        int actionMusic = intent.getIntExtra("action_music",0);
        Intent intentService = new Intent(context,MyService.class);
        intentService.putExtra("action_music_service",actionMusic);
        context.startService(intentService);
    }
}