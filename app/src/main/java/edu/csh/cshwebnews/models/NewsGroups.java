/*
 * Represents available newsgroups
 * https://github.com/grantovich/CSH-WebNews/wiki/API%3A-Retrieving-Newsgroups
 *
 * Peter Zujko
 */

package edu.csh.cshwebnews.models;

import java.util.List;

public class NewsGroups {

    private List<NewsGroup> newsgroups;

    private Meta meta;

    public List<NewsGroup> getNewsGroupList(){
        return newsgroups;
    }

    /**
     * @return  the time WebNews last performed a successful background sync of all newsgroups.
     */
    public String getLastSyncAt(){
        return meta.getLastSyncAt();
    }

    public class NewsGroup {

        private String id;

        private String description;

        private Integer max_unread_level;

        private String newest_post_at;

        private String oldest_post_at;

        private Boolean posting_allowed;

        private Integer unread_count;

        /**
         * @return The unique ID of the newsgroup on WebNews.
         */
        public String getId() {
            return id;
        }

        /**
         * @return The short description (typically a user-friendly "title") of the newsgroup,
         * or null if the news server has no description configured.
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return The highest personal level among posts in the newsgroup that are unread for
         * the current user, or null if no posts are unread.
         */
        public Integer getMaxUnreadLevel() {
            return max_unread_level;
        }

        /**
         * @return When the newest post in the newsgroup was created, or null if there are no posts.
         */
        public String getNewestPostAt() {
            return newest_post_at;
        }

        /**
         * @return When the oldest post in the newsgroup was created, or null if there are no posts.
         */
        public String getOldestPostAt() {
            return oldest_post_at;
        }

        /**
         * @return Whether the newsgroup is accepting new posts.
         */
        public Boolean postingAllowed() {
            return posting_allowed;
        }

        /**
         * @return How many posts in the newsgroup are unread for the current user.
         */
        public Integer getUnreadCount() {
            return unread_count;
        }
    }

    private class Meta {

        private String last_sync_at;

        public String getLastSyncAt() {
            return last_sync_at;
        }
    }

}
