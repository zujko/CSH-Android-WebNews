package edu.csh.cshwebnews.models.requests;

public class UnreadRequestBody {
    final String post_ids;

    public UnreadRequestBody(String postIds) {
        this.post_ids = postIds;
    }
}
