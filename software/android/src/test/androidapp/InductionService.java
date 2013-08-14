package test.androidapp;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
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
