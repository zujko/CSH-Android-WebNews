package edu.csh.cshwebnews;

import android.app.Application;
import android.util.Log;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import net.danlew.android.joda.JodaTimeAndroid;

public class WebNewsApplication extends Application {

    public static JobManager jobManager;

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        createJobManager();
    }

    private void createJobManager() {
        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOB MANAGER";
                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG + " ERROR", String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG + " ERROR", String.format(text, args));
                    }
                })
                .minConsumerCount(1)
                .maxConsumerCount(5)
                .loadFactor(3)
                .consumerKeepAlive(120)
                .build();

        jobManager = new JobManager(this, configuration);

    }

    public static JobManager getJobManager() {
        return jobManager;
    }
}
