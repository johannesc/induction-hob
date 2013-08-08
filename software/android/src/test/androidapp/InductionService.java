package test.androidapp;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class InductionService extends IOIOService {
    private static final String LOG_TAG = "InductionService";
    private final IBinder binder = new InductionBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.w(LOG_TAG, "onStart");
        super.onStart(intent, startId);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (intent != null && intent.getAction() != null
                && intent.getAction().equals("stop")) {
            // User clicked the notification. Need to stop the service.
            nm.cancel(0);
            stopSelf();
        } else {
            // Service starting. Create a notification.
            Notification notification = new Notification(
                    R.drawable.ic_launcher, "Induction Hob running",
                    System.currentTimeMillis());
            notification
                    .setLatestEventInfo(this, "Induction Hob", "Click to stop",
                            PendingIntent.getService(this, 0, new Intent(
                                    "stop", null, this, this.getClass()), 0));
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            nm.notify(0, notification);
        }
        Log.w(LOG_TAG, "onStart - done");
    }

    public class InductionBinder extends Binder {
        public InductionService getService() {
            return InductionService.this;
        }
    }

    InductionController inductionController = new InductionController();

    @Override
    protected IOIOLooper createIOIOLooper() {
        Log.w(LOG_TAG, "createIOIOLooper");
        return inductionController.createLooper();
    }

    public InductionController getInductionController() {
        Log.w(LOG_TAG, "getInductionController");
        return inductionController;
    }
}
