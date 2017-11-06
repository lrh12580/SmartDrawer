package com.isaac.smartdrawer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Isaac on 2016/7/31 0031.
 */
public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SyncService.setServiceAlarm(context, SettingsActivity.canSync(context));
        SyncService.setSmartAlert(context, SettingsActivity.canAlert(context));
    }
}
