package edu.csh.cshwebnews.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import edu.csh.cshwebnews.authentication.WebNewsAccountAuthenticator;

public class WebNewsAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new WebNewsAccountAuthenticator(this).getIBinder();
    }
}
