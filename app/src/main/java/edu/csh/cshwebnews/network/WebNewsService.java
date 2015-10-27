/*
 * Represents the WebNews api v1 as an interface
 * https://github.com/grantovich/CSH-WebNews/wiki/API-v1
 *
 * Peter Zujko
 */

package edu.csh.cshwebnews.network;

import com.squareup.okhttp.Response;

import edu.csh.cshwebnews.models.AccessToken;
import edu.csh.cshwebnews.models.NewsGroups;
import edu.csh.cshwebnews.models.Post;
import edu.csh.cshwebnews.models.RetrievingPosts;
import edu.csh.cshwebnews.models.User;
import edu.csh.cshwebnews.models.requests.CancelPostRequestBody;
import edu.csh.cshwebnews.models.requests.PostRequestBody;
import edu.csh.cshwebnews.models.requests.StickyRequestBody;
import edu.csh.cshwebnews.models.requests.UnreadRequestBody;
import retrofit.Call;
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
    Call<User> getUser();

    @GET("/newsgroups")
    Call<NewsGroups> getNewsGroups();

    @GET("/posts/{id}")
    Call<Post> getSinglePost(@Path("id") String id,
                 @Query("as_thread") Boolean asThread);

    @GET("/posts")
    Call<RetrievingPosts> getPosts(@Query("as_meta") String asMeta,
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


    @GET("/posts/{id}")
    RetrievingPosts blockingIdGetPosts(@Path("id") String id,
                                       @Query("as_meta") String asMeta,
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
    Call<Response> post(@Body PostRequestBody body);


    @DELETE("/posts/{id}")
    Call<Response> deletePost(@Path("id") String id,
                    @Body CancelPostRequestBody body);

    @DELETE("/unreads")
    Call<Response> markPostRead(@Body UnreadRequestBody body);

    @POST("/unreads")
    Call<Response> markPostUnread(@Body UnreadRequestBody body);

    @POST("/posts/{id}/star")
    Call<Response> starPost(@Path("id") String id);

    @DELETE("/posts/{id}/star")
    Call<Response> unstarPost(@Path("id") String id);

    @PATCH("/posts/{id}/sticky")
    Call<Response> stickyPost(@Path("id") String id,
                    @Body StickyRequestBody body);

    @POST("/oauth/token")
    Call<AccessToken> getAccessToken(@Query("grant_type") String grantType,
                               @Query("code") String code,
                               @Query("redirect_uri") String redirectUri,
                               @Query("client_id") String clientId,
                               @Query("client_secret") String clientSecret);

    @POST("/oauth/token")
    Call<AccessToken> refreshAccessToken(@Query("grant_type") String grantType,
                            @Query("refresh_token") String refreshToken);
}
