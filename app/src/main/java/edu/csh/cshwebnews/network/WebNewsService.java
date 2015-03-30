package edu.csh.cshwebnews.network;

import edu.csh.cshwebnews.models.NewsGroups;
import edu.csh.cshwebnews.models.Post;
import edu.csh.cshwebnews.models.RetrievingPosts;
import edu.csh.cshwebnews.models.User;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Service to interact with the WebNews API
 */
public interface WebNewsService {


    @GET("/user")
    void getUser(Callback<User> userCallback);

    @GET("/newsgroups")
    void getNewsGroups(Callback<NewsGroups> newsGroupsCallback);

    @GET("/posts/{id}")
    void getSinglePost(@Path("id") String id,
                 @Query("as_thread") Boolean asThread, Callback<Post> postCallback);

    @GET("/posts")
    void getPosts(@Query("as_meta") Boolean asMeta,
                  @Query("as_threads") Boolean asThreads,
                  @Query("authors") String authors,
                  @Query("keywords") String keywords,
                  @Query("keywords_match") String keywords_match,
                  @Query("limit") Integer limit,
                  @Query("min_unread_level") Integer minUnreadLevel,
                  @Query("newsgroup_ids") String newsgroupIds,
                  @Query("offset") Integer offset,
                  @Query("only_roots") Boolean onlyRoots,
                  @Query("only_starred") Boolean onlyStarred,
                  @Query("only_sticky") Boolean onlySticky,
                  @Query("reverse_order") Boolean reverseOrder,
                  @Query("since") String sinceDate,
                  @Query("until") String untilDate, Callback<RetrievingPosts> retrievingPostsCallback);

    @POST("/posts")
    void post(@Query("body") String body,
              @Query("followup_newsgroup_id") Integer followUpNewsGroupId,
              @Query("newsgroup_ids") String newsgroupIds,
              @Query("parent_id") Integer parentId,
              @Query("posting_host") String postingHost,
              @Query("subject") String subject, Callback<Response> responseCallback);

    @DELETE("/posts/{id}")
    void deletePost(@Path("id") String id,
                    @Query("posting_host") String postingHost,
                    @Query("reason") String reason, Callback<Response> responseCallback);

    @DELETE("/unreads")
    void markPostRead(@Query("post_ids") String postIds, Callback<Response> responseCallback);

    @POST("/unreads")
    void markPostUnread(@Query("post_ids") String postIds, Callback<Response> responseCallback);

    @POST("/posts/{id}/star")
    void starPost(@Path("id") String id, Callback<Response> responseCallback);

    @DELETE("/posts/{id}/star")
    void unstarPost(@Path("id") String id, Callback<Response> responseCallback);

    @PATCH("/posts/{id}/sticky")
    void stickyPost(@Query("expires_at") String expireDate,
                    @Path("id") String id, Callback<Response> responseCallback);

}
