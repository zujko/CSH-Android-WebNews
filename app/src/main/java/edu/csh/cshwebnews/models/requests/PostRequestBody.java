package edu.csh.cshwebnews.models.requests;


public class PostRequestBody {
    final String subject;
    final String newsgroup_ids;
    final String body;
    final Integer parent_id;
    final Integer followup_newsgroup_id;
    final String posting_host;


    public PostRequestBody(String subject, String newsgroupIds, String body, Integer parentId, Integer followupId, String postingHost) {
        this.subject = subject;
        this.newsgroup_ids = newsgroupIds;
        this.body = body;
        this.parent_id = parentId;
        this.followup_newsgroup_id = followupId;
        this.posting_host = postingHost;
    }


}
