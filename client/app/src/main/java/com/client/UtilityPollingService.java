package com.client;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class UtilityPollingService extends Service {

    private int secondsActLocSel;
    private boolean stopTimerActLocSel;
    private int secondsUpdateLoc;
    private boolean stopTimerUpdateLoc;
    private int secondsActRideEjoy;
    private boolean stopTimerActRideEjoy;
    private int secondsActRideOTP;
    private boolean stopTimerActRideOTP;

    boolean stop = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null && intent.getAction().equals("00")) {
            final int fixedTimeUpdateLoc = 30;

            secondsActLocSel = fixedTimeUpdateLoc;
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d(" ActivityRideHome.getInstance().sendLocation() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideHome.getInstance().sendLocation();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d(" ActivityRideHome.getInstance().sendLocation() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("01")) {

            final int fixedTimeUpdateLoc = 30;
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
                        Log.d("ActivityRideHome.getInstance().getAvailableVehicle() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideHome.getInstance().getAvailableVehicle();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideHome.getInstance().getAvailableVehicle() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("02")) {
            final int fixedTimeUpdateLoc = 30;
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
                        Log.d(" ActivityRideRequest.getInstance().checkStatus() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideRequest.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d(" ActivityRideRequest.getInstance().checkStatus() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("03")) {
            final int fixedTimeUpdateLoc = 30;
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
                        Log.d("ActivityRideOTP.getInstance().checkStatus() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideOTP.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideOTP.getInstance().checkStatus() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("04")) {
            final int fixedTimeUpdateLoc = 30;
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
                        Log.d("ActivityRideInProgress.getInstance().checkStatus() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideInProgress.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideInProgress.getInstance().checkStatus() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("05")) {
            final int fixedTimeUpdateLoc = 30;
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
                        Log.d("ActivityRideEnded.getInstance().checkStatus() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideEnded.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideEnded.getInstance().checkStatus() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

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
        stop = true;

    }
}
