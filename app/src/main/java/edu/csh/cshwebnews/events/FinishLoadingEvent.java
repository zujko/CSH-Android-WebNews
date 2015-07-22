package edu.csh.cshwebnews.events;

public class FinishLoadingEvent {
    public final boolean success;
    public final String errorMessage;

    public FinishLoadingEvent(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
}
