package com.g2minhle.phonewhereru;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

/**
 * Created by root on 5/26/15.
 */
public class TileEventReceiver extends BroadcastReceiver {

    private static Ringtone ringtone;
    private static Vibrator vibrator;

    @Override
    public void onReceive(Context context, Intent intent) {
        long[] pattern = {0, 1000, 100};

        if (ringtone == null){
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
        }

        if(vibrator == null){
            vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (intent.getAction() == "com.microsoft.band.action.ACTION_TILE_OPENED") {
            ringtone.play();
            vibrator.vibrate(pattern,0);
        }
        else if (intent.getAction() == "com.microsoft.band.action.ACTION_TILE_BUTTON_PRESSED") {
            // handle button pressed event
        }
        else if (intent.getAction() == "com.microsoft.band.action.ACTION_TILE_CLOSED") {
            // handle tile closed event
            ringtone.stop();
            vibrator.cancel();
        }
    }
}