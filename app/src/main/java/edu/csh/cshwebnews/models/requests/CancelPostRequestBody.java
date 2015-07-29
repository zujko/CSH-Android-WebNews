package edu.csh.cshwebnews.models.requests;

public class CancelPostRequestBody {
    final String reason;
    final String posting_host;

    public CancelPostRequestBody(String reason, String postingHost) {
        this.reason = reason;
        this.posting_host = postingHost;
    }
}
