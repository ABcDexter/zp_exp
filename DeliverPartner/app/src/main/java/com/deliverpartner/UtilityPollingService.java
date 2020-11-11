package com.deliverpartner;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class UtilityPollingService extends Service {

    boolean stop = false;
    private int secondsActLocSel;
    private boolean stopTimerActLocSel;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null && intent.getAction().equals("00")) {
            final int fixedTimeUpdateLoc = 45;
            secondsActLocSel = fixedTimeUpdateLoc;
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("ActivityVerifyDLDetails.getInstance().isDriverVerified() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityVerifyDLDetails.getInstance().isDriverVerified();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityVerifyDLDetails.getInstance().isDriverVerified() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

//polling for
        if (intent != null && intent.getAction() != null && intent.getAction().equals("01")) {

            final int fixedTimeUpdateLoc = 45;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;
            //stopTimer2 = stopTimerFlag2;
/*
            if (stop) {
*/
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("ActivityHome.getInstance().sendLocation(); in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityHome.getInstance().sendLocation();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityHome.getInstance().sendLocation(); in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("02")) {

            final int fixedTimeUpdateLoc = 45;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;
            //stopTimer2 = stopTimerFlag2;
/*
            if (stop) {
*/
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("ActivityHome.getInstance().getStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityHome.getInstance().getStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityHome.getInstance().getStatus(); in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
