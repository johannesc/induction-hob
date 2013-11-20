package test.androidapp;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class InductionService extends IOIOService {
    private static final String LOG_TAG = "InductionService";
    private final IBinder binder = new InductionBinder();
    private static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.w(LOG_TAG, "onStart");
        super.onStart(intent, startId);
        createNotification();
        Log.w(LOG_TAG, "onStart - done");
    }

    public class InductionBinder extends Binder {
        public InductionService getService() {
            return InductionService.this;
        }
    }

    @Override
    public void onDestroy() {
        Log.w(LOG_TAG, "onDestroy");
        removeNotification();
        super.onDestroy();
        Log.w(LOG_TAG, "onDestroy - done");
    };

    InductionController inductionController = new InductionController();

    @Override
    protected IOIOLooper createIOIOLooper() {
        Log.w(LOG_TAG, "createIOIOLooper");
        return inductionController.createLooper();
    }

    private void createNotification() {
        notificationBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle("Induction Hob")
                .setContentText("Click to show");

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    //TODO the service should perhaps call this?
    public void updateNotification(String text) {
        notificationBuilder.setContentText(text);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public InductionController getInductionController() {
        Log.w(LOG_TAG, "getInductionController");
        return inductionController;
    }
}
