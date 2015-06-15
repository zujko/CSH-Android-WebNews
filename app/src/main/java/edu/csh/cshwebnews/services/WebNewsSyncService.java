package edu.csh.cshwebnews.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import edu.csh.cshwebnews.network.WebNewsSyncAdapter;

public class WebNewsSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static WebNewsSyncAdapter sWebNewsSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("WebNewsSyncService", "onCreate - WebNewsSyncService");
        synchronized (sSyncAdapterLock) {
            if (sWebNewsSyncAdapter == null) {
                sWebNewsSyncAdapter = new WebNewsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sWebNewsSyncAdapter.getSyncAdapterBinder();
    }
}
