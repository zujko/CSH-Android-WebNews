package edu.csh.cshwebnews.events;

import android.content.Intent;

public class FinishLoginEvent {
    public final Intent intent;
    public final boolean success;
    public final String reason;

    public FinishLoginEvent(boolean success, String reason, Intent args) {
        this.success = success;
        this.reason = reason;
        this.intent = args;
    }
}
