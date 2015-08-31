/*
 * Represents the WebNews api v1 as an interface
 * https://github.com/grantovich/CSH-WebNews/wiki/API-v1
 *
 * Peter Zujko
 */

package edu.csh.cshwebnews.network;

import edu.csh.cshwebnews.models.AccessToken;
import edu.csh.cshwebnews.models.NewsGroups;
import edu.csh.cshwebnews.models.Post;
import edu.csh.cshwebnews.models.requests.PostRequestBody;
import edu.csh.cshwebnews.models.RetrievingPosts;
import edu.csh.cshwebnews.models.User;
import edu.csh.cshwebnews.models.requests.CancelPostRequestBody;
import edu.csh.cshwebnews.models.requests.StickyRequestBody;
import edu.csh.cshwebnews.models.requests.UnreadRequestBody;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface WebNewsService {

    String BASE_URL     = "https://webnews-staging.csh.rit.edu";
    String REDIRECT_URI = "webnewstest://data";


    @GET("/user")
    void getUser(Callback<User> userCallback);

    @GET("/user")
    User blockingGetUser();

    @GET("/newsgroups")
    void getNewsGroups(Callback<NewsGroups> newsGroupsCallback);

    @GET("/newsgroups")
    NewsGroups blockingGetNewsGroups();

    @GET("/posts/{id}")
    void getSinglePost(@Path("id") String id,
                 @Query("as_thread") Boolean asThread,
                       Callback<Post> postCallback);

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
                  @Query("until") String untilDate,
                  Callback<RetrievingPosts> retrievingPostsCallback);

    @GET("/posts")
    RetrievingPosts blockingGetPosts(@Query("as_meta") String asMeta,
                                     @Query("as_threads") Boolean asThreads,
                                     @Query("authors") String authors,
                                     @Query("keywords") String keywords,
                                     @Query("keywords_match") String keywords_match,
                                     @Query("limit") String limit,
                                     @Query("min_unread_level") String minUnreadLevel,
                                     @Query("newsgroup_ids") String newsgroupIds,
                                     @Query("offset") Integer offset,
                                     @Query("only_roots") Boolean onlyRoots,
                                     @Query("only_starred") Boolean onlyStarred,
                                     @Query("only_sticky") Boolean onlySticky,
                                     @Query("reverse_order") String reverseOrder,
                                     @Query("since") String sinceDate,
                                     @Query("until") String untilDate);

    @POST("/posts")
    void post(@Body PostRequestBody body,
              Callback<Response> responseCallback);

    @POST("/posts")
    Response blockingPost(@Body PostRequestBody body);

    @DELETE("/posts/{id}")
    void deletePost(@Path("id") String id,
                    @Body CancelPostRequestBody body,
                    Callback<Response> responseCallback);

    @DELETE("/unreads")
    void markPostRead(@Body UnreadRequestBody body,
                      Callback<Response> responseCallback);

    @POST("/unreads")
    void markPostUnread(@Body UnreadRequestBody body,
                        Callback<Response> responseCallback);

    @POST("/posts/{id}/star")
    void starPost(@Path("id") String id,
                  Callback<Response> responseCallback);

    @DELETE("/posts/{id}/star")
    void unstarPost(@Path("id") String id,
                    Callback<Response> responseCallback);

    @PATCH("/posts/{id}/sticky")
    void stickyPost(@Path("id") String id,
                    @Body StickyRequestBody body,
                    Callback<Response> responseCallback);

    @POST("/oauth/token")
    void getAccessToken(@Query("grant_type") String grantType,
                               @Query("code") String code,
                               @Query("redirect_uri") String redirectUri,
                               @Query("client_id") String clientId,
                               @Query("client_secret") String clientSecret,
                               Callback<AccessToken> accessTokenCallback);
    @POST("/oauth/token")
    AccessToken blockingGetAccessToken(@Query("grant_type") String grantType,
                        @Query("code") String code,
                        @Query("redirect_uri") String redirectUri,
                        @Query("client_id") String clientId,
                        @Query("client_secret") String clientSecret);

    @POST("/oauth/token")
    void refreshAccessToken(@Query("grant_type") String grantType,
                            @Query("refresh_token") String refreshToken,
                            Callback<AccessToken> accessTokenCallback);

    @POST("/oauth/token")
    AccessToken blockingRefreshAccessToken(@Query("grant_type") String grantType,
                                           @Query("refresh_token") String refreshToken);
}
