package com.dev.smartlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by daniele on 09/04/2015.
 */

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            Intent i=new Intent(context,ChooseAppActivity.KillOnLock.class);
            i.setAction(Intent.ACTION_SCREEN_OFF);
            context.startService(i);
        }
    }
}