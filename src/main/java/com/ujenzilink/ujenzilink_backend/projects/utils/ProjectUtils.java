package com.ujenzilink.ujenzilink_backend.projects.utils;

import com.ujenzilink.ujenzilink_backend.projects.models.Project;

import java.util.Random;

public class ProjectUtils {
    private static final Random random = new Random();

    /**
     * Increments the impressions count for a project.
     * This should be called when a project is fetched/viewed.
     * 
     * @param project The project to increment impressions for
     */
    public static void incrementImpressions(Project project) {
        if (project != null) {
            Integer currentImpressions = project.getImpressions();
            if (currentImpressions == null) {
                currentImpressions = 0;
            }
            project.setImpressions(currentImpressions + 1);
        }
    }

    /**
     * Calculates a randomized progress percentage based on the current stage and
     * total stages.
     * This avoids 0% for the first stage and prevents duplicate percentages between
     * stages.
     * 
     * @param currentStageIndex The index (ordinal) of the current construction
     *                          stage.
     * @param totalStages       The total number of construction stages.
     * @return A randomized progress percentage between 1 and 100.
     */
    public static int calculateRandomizedProgress(int currentStageIndex, int totalStages) {
        return calculateRandomizedProgress(currentStageIndex, totalStages, random.nextLong());
    }

    /**
     * Calculates a randomized progress percentage based on the current stage and
     * total stages.
     * Providing a seed ensures the result is stable for the same seed (e.g.,
     * projectId).
     * 
     * @param currentStageIndex The index (ordinal) of the current construction
     *                          stage.
     * @param totalStages       The total number of construction stages.
     * @param seed              A seed for the random number generator to ensure
     *                          stable results.
     * @return A randomized progress percentage.
     */
    public static int calculateRandomizedProgress(int currentStageIndex, int totalStages, long seed) {
        if (totalStages <= 0)
            return 0;

        Random seededRandom = new Random(seed);

        // Base progress calculation
        int baseProgress = (currentStageIndex * 100) / totalStages;

        // Randomize the offset (between 2 and 7 as suggested by the user)
        int randomOffset = 2 + seededRandom.nextInt(6); // random.nextInt(6) gives 0-5, so 2+0..5 = 2..7

        int randomizedProgress = baseProgress + randomOffset;

        return Math.min(randomizedProgress, 100);
    }

    public static String formatEnumName(String enumName) {
        if (enumName == null || enumName.isEmpty()) {
            return "";
        }
        String name = enumName.replace("_", " ").toLowerCase();
        String[] words = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
