package com.example.driver;

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

        /*if (intent != null && intent.getAction() != null && intent.getAction().equals("2")) {
            final int fixedTimeUpdateLoc = 30;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;

            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("VehicleList.getInstance().getData() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        VehicleList.getInstance().getData();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("VehicleList.getInstance().getData() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });

        }*/

        if (intent != null && intent.getAction() != null && intent.getAction().equals("4")) {
           /* if (stop) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ActivityRideAccepted.getInstance().rideStatus(); //call function!
                    }
                }, 10000);
            } else stopSelf();*/
            final int fixedTimeUpdateLoc = 30;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;

            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("ActivityRideAccepted.getInstance().rideStatus() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideAccepted.getInstance().rideStatus(); //call function!

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideAccepted.getInstance().rideStatus() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("6")) {
            final int fixedTimeUpdateLoc = 30;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;

            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("ActivityHome.getInstance().driverRideCheck() in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityHome.getInstance().driverRideCheck();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityHome.getInstance().driverRideCheck() in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });

        }
        /*if (intent != null && intent.getAction() != null && intent.getAction().equals("7")) {
            final int fixedTimeUpdateLoc = 30;
            secondsActLocSel = fixedTimeUpdateLoc;
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("ActivityRideAccepted.getInstance().rideStatus(); in secondsUpdateLoc < 0", "Value of secondsActLocSel: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideAccepted.getInstance().rideStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideAccepted.getInstance().rideStatus(); in secondsUpdateLoc == false ", "Value of secondsActLocSel: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }*/

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
