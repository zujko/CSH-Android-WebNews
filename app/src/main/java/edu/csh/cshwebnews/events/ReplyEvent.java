package edu.csh.cshwebnews.events;

public class ReplyEvent {
    public final String postId;
    public final String newsgroup;
    public final String subject;
    public final String body;
    public final String author;

    public ReplyEvent(String postId, String newsgroup, String subject, String body, String author) {
        this.postId = postId;
        this.newsgroup = newsgroup;
        this.subject = subject;
        this.body = body;
        this.author = author;
    }
}
