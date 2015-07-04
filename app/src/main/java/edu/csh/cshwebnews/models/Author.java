/*
 * Represents the author of a post.
 *
 * Peter Zujko
 */

package edu.csh.cshwebnews.models;

public class Author {

    private String name;

    private String email;

    private String avatar_url;

    private String raw;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    /**
     * @return Raw content of "From" header.
     */
    public String getRaw() {
        return raw;
    }
}
