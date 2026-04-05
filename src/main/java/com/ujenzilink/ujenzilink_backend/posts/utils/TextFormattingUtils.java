package com.ujenzilink.ujenzilink_backend.posts.utils;

public class TextFormattingUtils {

    /**
     * Normalizes multiline text content to preserve formatting consistently.
     * Actions:
     * 1. Standardizes all line endings (e.g., \r\n, \r) to \n (Unix style).
     * 2. Trims leading and trailing whitespace from the overall string.
     * 3. (Optional) Collapses 3+ consecutive newlines into 2 to prevent whitespace spamming.
     * 
     * @param content The raw multi-line string content.
     * @return The normalized string, or an empty string if null.
     */
    public static String normalizeContent(String content) {
        if (content == null) {
            return "";
        }

        // 1. Standardize line endings (\r\n or \r  -> \n)
        String normalized = content.replace("\r\n", "\n").replace("\r", "\n");

        // 2. Collapse excessive newlines (3+ newlines -> 2)
        // This regex finds 3 or more consecutive \n and replaces with 2 \n.
        normalized = normalized.replaceAll("\n{3,}", "\n\n");

        // 3. Trim overall string
        return normalized.trim();
    }
}
