package edu.csh.cshwebnews.events;

public class NewPostEvent {
    public final boolean success;
    public final String errorMessage;

    public NewPostEvent(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
}
