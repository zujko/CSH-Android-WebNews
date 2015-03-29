package edu.csh.cshwebnews.models;

import java.util.List;

/**
 * TODO: Add id class which contains ancestor ids, child ids,..etc
 */
public class RetrievingPosts {

    private Posts posts;

    private Meta meta;

    /**
     * @return The IDs of the posts that matched the query.
     */
    public List<Integer> getListOfMatchedIds(){
        return meta.getMatchedIds();
    }

    /**
     * @return The total number of posts that match the query, disregarding limit and offset.
     */
    public Integer getMatchingPostsTotal(){
        return meta.getTotal();
    }

    public List<Post> getListOfPosts(){
        return posts.getPosts();
    }

    private class Meta {

        private List<Integer> matched_ids;

        private Integer total;

        public List<Integer> getMatchedIds() {
            return matched_ids;
        }

        public Integer getTotal() {
            return total;
        }
    }

    private class Posts {

        private List<Post> posts;

        public List<Post> getPosts() {
            return posts;
        }
    }

}
