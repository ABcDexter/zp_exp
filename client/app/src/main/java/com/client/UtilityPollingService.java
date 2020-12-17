package com.client;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.client.deliver.ActivityDeliverConfirm;
import com.client.rent.ActivityRentEnded;
import com.client.rent.ActivityRentHome;
import com.client.rent.ActivityRentInProgress;
import com.client.rent.ActivityRentOTP;
import com.client.rent.ActivityRentRequest;
import com.client.ride.ActivityRideEnded;
import com.client.ride.ActivityRideHome;
import com.client.ride.ActivityRideInProgress;
import com.client.ride.ActivityRideOTP;
import com.client.ride.ActivityRideRequest;

public class UtilityPollingService extends Service {
    // private int secondsActLocSel;
    // private boolean stopTimerActLocSel;

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
//polling for auth-location-update API in ride section
        if (intent != null && intent.getAction() != null && intent.getAction().equals("00")) {
            final int fixedTimeUpdateLoc = 45;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;
            //stopTimer2 = stopTimerFlag2;
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("ActivityWelcome.getInstance().sendLocation() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityWelcome.getInstance().sendLocation();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityWelcome.getInstance().sendLocation() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }
//polling for user-is-driver-av in RIDE section
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
                        Log.d("ActivityRideHome.getInstance().isDriverAv() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideHome.getInstance().isDriverAv();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideHome.getInstance().isDriverAv() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }
//polling for user-trip-get-status API in RIDE section
        if (intent != null && intent.getAction() != null && intent.getAction().equals("02")) {
            final int fixedTimeUpdateLoc = 30;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;
            //stopTimer2 = stopTimerFlag2;

            //seconds = 30;
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
                        Log.d("ActivityRideOTP.getInstance().checkStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideOTP.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideOTP.getInstance().checkStatus() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("04")) {
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
                        Log.d("ActivityRideInProgress.getInstance().checkStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideInProgress.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideInProgress.getInstance().checkStatus() in seconds == false ", "Value of seconds: " + secondsActLocSel);

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
                        Log.d("ActivityRideEnded.getInstance().checkStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRideEnded.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRideEnded.getInstance().checkStatus() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

//polling for rental section

//polling for auth-vehicle-get-avail in rent section
        if (intent != null && intent.getAction() != null && intent.getAction().equals("11")) {
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
                        Log.d("ActivityRentHome.getInstance().getAvailableVehicle(); in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRentHome.getInstance().getAvailableVehicle();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRentHome.getInstance().getAvailableVehicle(); in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

//polling for user-trip-get-status API in rent section
        if (intent != null && intent.getAction() != null && intent.getAction().equals("12")) {
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
                        Log.d(" ActivityRentRequest.getInstance().checkStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRentRequest.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d(" ActivityRentRequest.getInstance().checkStatus() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("13")) {
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
                        Log.d("ActivityRentOTP.getInstance().checkStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRentOTP.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRentOTP.getInstance().checkStatus() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("14")) {
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
                        Log.d("ActivityRentInProgress.getInstance().checkStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRentInProgress.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRentInProgress.getInstance().checkStatus() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals("15")) {
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
                        Log.d("ActivityRentEnded.getInstance().checkStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityRentEnded.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityRentEnded.getInstance().checkStatus() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

        //polling for user-is-agent-av in DELIVERY section
        /*if (intent != null && intent.getAction() != null && intent.getAction().equals("31")) {
            final int fixedTimeUpdateLoc = 45;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;
            //stopTimer2 = stopTimerFlag2;
*//*
            if (stop) {
*//*
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    secondsActLocSel--;
                    if (secondsActLocSel < 0) {
                        Log.d("ActivityFillDropAddress.getInstance().getAgent() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityFillDropAddress.getInstance().getAgent();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityFillDropAddress.getInstance().getAgent() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }
*/
        //polling for user-delivery-get-status API in DELIVERY section
        if (intent != null && intent.getAction() != null && intent.getAction().equals("32")) {
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
                        Log.d("ActivityDeliverConfirm.getInstance().checkStatus() in seconds < 0", "Value of seconds: " + secondsActLocSel);

                        stopTimerActLocSel = true;
                        ActivityDeliverConfirm.getInstance().checkStatus();

                    } else {
                        stopTimerActLocSel = false;
                    }

                    if (stopTimerActLocSel == false) {
                        Log.d("ActivityDeliverConfirm.getInstance().checkStatus() in seconds == false ", "Value of seconds: " + secondsActLocSel);

                        handler.postDelayed(this, 1000);
                    } else {
                        stopSelf();
                    }
                }
            });
        }

//polling for user-delivery-get-status API in DELIVERY section
       /* if (intent != null && intent.getAction() != null && intent.getAction().equals("33")) {
            final int fixedTimeUpdateLoc = 45;
            //final boolean stopTimerFlag2 = false;
            secondsActLocSel = fixedTimeUpdateLoc;
            //stopTimer2 = stopTimerFlag2;
*//*
            if (stop) {
*/
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop = true;

    }
}
