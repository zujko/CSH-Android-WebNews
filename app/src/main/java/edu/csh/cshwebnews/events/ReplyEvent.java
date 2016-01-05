package edu.csh.cshwebnews.events;

public class ReplyEvent {
    public final String postId;
    public final String newsgroup;
    public final String subject;

    public ReplyEvent(String postId, String newsgroup, String subject) {
        this.postId = postId;
        this.newsgroup = newsgroup;
        this.subject = subject;
    }
}
