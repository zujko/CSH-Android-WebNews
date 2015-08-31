/*
 * Represents posts
 * TODO: Add id class which contains ancestor ids, child ids,..etc
 * https://github.com/grantovich/CSH-WebNews/wiki/API%3A-Retrieving-Posts
 *
 * Peter Zujko
 */

package edu.csh.cshwebnews.models;

import java.util.List;

public class RetrievingPosts {

    private List<Post> posts;

    private Meta meta;

    private List<Post> descendants;

    /**
     * @return The IDs of the posts that matched the query.
     */
    public List<String> getListOfMatchedIds(){
        return meta.getMatchedIds();
    }

    /**
     * @return The total number of posts that match the query, disregarding limit and offset.
     */
    public Integer getMatchingPostsTotal(){
        return meta.getTotal();
    }

    public List<Post> getListOfPosts(){
        return posts;
    }

    public List<Post> getListOfDescendants() { return descendants; }

    private class Meta {

        private List<String> matched_ids;

        private Integer total;

        public List<String> getMatchedIds() {
            return matched_ids;
        }

        public Integer getTotal() {
            return total;
        }
    }

}
