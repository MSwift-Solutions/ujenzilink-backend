package com.ujenzilink.ujenzilink_backend.posts.utils;

import com.ujenzilink.ujenzilink_backend.posts.models.Post;

public class PostUtils {

    /**
     * Increments the impressions count for a post.
     * This should be called when a post is fetched/viewed.
     * 
     * @param post The post to increment impressions for
     */
    public static void incrementImpressions(Post post) {
        if (post != null) {
            Integer currentImpressions = post.getImpressions();
            if (currentImpressions == null) {
                currentImpressions = 0;
            }
            post.setImpressions(currentImpressions + 1);
        }
    }

    /**
     * Increments the views count for a post.
     * This should be called when a post is fully viewed.
     * 
     * @param post The post to increment views for
     */
    public static void incrementViews(Post post) {
        if (post != null) {
            Integer currentViews = post.getViews();
            if (currentViews == null) {
                currentViews = 0;
            }
            post.setViews(currentViews + 1);
        }
    }
}
