package edu.csh.cshwebnews.models.requests;

public class StickyRequestBody {
    final String expires_at;

    public StickyRequestBody(String expiresAt) {
        this.expires_at = expiresAt;
    }
}
