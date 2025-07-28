package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.service.dto.GoalAchievementDTO;
import com.mycompany.myapp.service.dto.GoalProgressDTO;
import com.mycompany.myapp.service.dto.MoodGoalDTO;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing mood goals.
 */
@Service
@Transactional
public class MoodGoalsService {

    private static final Logger LOG = LoggerFactory.getLogger(MoodGoalsService.class);

    private final MoodEntryRepository moodEntryRepository;

    // In-memory storage for goals (in a real application, this would be a database)
    private final Map<Long, MoodGoalDTO> goals = new HashMap<>();
    private final Map<Long, Integer> userPoints = new HashMap<>();

    public MoodGoalsService(MoodEntryRepository moodEntryRepository) {
        this.moodEntryRepository = moodEntryRepository;
    }

    /**
     * Create a new mood goal.
     *
     * @param goalDTO the goal to create
     * @return the created goal
     */
    public MoodGoalDTO createMoodGoal(MoodGoalDTO goalDTO) {
        LOG.debug("Request to create MoodGoal : {}", goalDTO);

        // Generate a unique ID
        Long goalId = System.currentTimeMillis();
        goalDTO.setId(goalId);
        goalDTO.setActive(true);
        goalDTO.setCompleted(false);

        goals.put(goalId, goalDTO);

        return goalDTO;
    }

    /**
     * Get goal progress for the current user.
     *
     * @return the goal progress
     */
    @Transactional(readOnly = true)
    public GoalProgressDTO getGoalProgress() {
        LOG.debug("Request to get goal progress");

        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        // Find active goals for the current user
        List<MoodGoalDTO> userGoals = goals
            .values()
            .stream()
            .filter(goal -> goal.getUserId() != null && goal.getActive())
            .collect(Collectors.toList());

        if (userGoals.isEmpty()) {
            return createEmptyProgress();
        }

        // For simplicity, we'll focus on the first active goal
        MoodGoalDTO activeGoal = userGoals.get(0);

        GoalProgressDTO progress = new GoalProgressDTO();
        progress.setGoalId(activeGoal.getId());
        progress.setGoalType(activeGoal.getType());
        progress.setTarget(activeGoal.getTarget());

        // Calculate progress based on goal type
        if ("HAPPY_DAYS".equals(activeGoal.getType())) {
            int completed = calculateHappyDaysThisWeek();
            progress.setCompleted(completed);
            progress.setPercentage(activeGoal.getTarget() > 0 ? (completed * 100) / activeGoal.getTarget() : 0);
        } else if ("STREAK".equals(activeGoal.getType())) {
            int currentStreak = calculateCurrentHappyStreak();
            progress.setCompleted(currentStreak);
            progress.setCurrentStreak(currentStreak);
            progress.setPercentage(activeGoal.getTarget() > 0 ? (currentStreak * 100) / activeGoal.getTarget() : 0);
        }

        return progress;
    }

    /**
     * Check if a goal has been achieved.
     *
     * @return the goal achievement information
     */
    @Transactional(readOnly = true)
    public GoalAchievementDTO checkGoalAchievement() {
        LOG.debug("Request to check goal achievement");

        GoalProgressDTO progress = getGoalProgress();
        GoalAchievementDTO achievement = new GoalAchievementDTO();

        if (progress.getCompleted() != null && progress.getTarget() != null && progress.getCompleted() >= progress.getTarget()) {
            achievement.setAchieved(true);
            achievement.setGoalCompleted(true);
            achievement.setPointsEarned(calculatePointsEarned(progress.getGoalType(), progress.getTarget()));
            achievement.setAchievementMessage("Congratulations! You've achieved your goal!");

            // Mark goal as completed
            if (progress.getGoalId() != null) {
                MoodGoalDTO goal = goals.get(progress.getGoalId());
                if (goal != null) {
                    goal.setCompleted(true);
                    goal.setActive(false);
                }
            }

            // Award points
            String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = getUserIdFromLogin(currentUserLogin);
            if (userId != null) {
                int currentPoints = userPoints.getOrDefault(userId, 0);
                userPoints.put(userId, currentPoints + achievement.getPointsEarned());
            }
        } else {
            achievement.setAchieved(false);
            achievement.setGoalCompleted(false);
            achievement.setPointsEarned(0);
            achievement.setAchievementMessage("Keep going! You're making progress!");
        }

        return achievement;
    }

    /**
     * Create multiple goals.
     *
     * @param goalsData the goals data
     * @return the created goals
     */
    public List<MoodGoalDTO> createMultipleGoals(List<Map<String, String>> goalsData) {
        LOG.debug("Request to create multiple goals");

        List<MoodGoalDTO> createdGoals = new ArrayList<>();

        for (Map<String, String> goalData : goalsData) {
            MoodGoalDTO goal = new MoodGoalDTO();
            goal.setType(goalData.get("goal_type"));
            goal.setTarget(Integer.parseInt(goalData.get("target")));
            goal.setTimeframe(goalData.get("timeframe"));
            goal.setActive(true);
            goal.setCompleted(false);

            // Set user ID
            String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = getUserIdFromLogin(currentUserLogin);
            goal.setUserId(userId);

            createdGoals.add(createMoodGoal(goal));
        }

        return createdGoals;
    }

    /**
     * Trigger goal reminder.
     *
     * @return the reminder message
     */
    @Transactional(readOnly = true)
    public String triggerGoalReminder() {
        LOG.debug("Request to trigger goal reminder");

        GoalProgressDTO progress = getGoalProgress();

        if (progress.getCompleted() != null && progress.getTarget() != null) {
            int remaining = progress.getTarget() - progress.getCompleted();
            if (remaining > 0) {
                return "You're " + remaining + " away from your goal! Keep logging your moods to achieve it!";
            }
        }

        return "Great job! You're on track with your goals!";
    }

    /**
     * Get goal history for the current user.
     *
     * @return the goal history
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getGoalHistory() {
        LOG.debug("Request to get goal history");

        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdFromLogin(currentUserLogin);

        Map<String, Object> history = new HashMap<>();

        // Get completed goals
        List<MoodGoalDTO> completedGoals = goals
            .values()
            .stream()
            .filter(goal -> Objects.equals(goal.getUserId(), userId) && Boolean.TRUE.equals(goal.getCompleted()))
            .collect(Collectors.toList());

        history.put("completed_goals", completedGoals);
        history.put("total_completed", completedGoals.size());

        // Get achievement statistics
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total_goals_created", goals.values().stream().filter(goal -> Objects.equals(goal.getUserId(), userId)).count());
        statistics.put(
            "active_goals",
            goals
                .values()
                .stream()
                .filter(goal -> Objects.equals(goal.getUserId(), userId) && Boolean.TRUE.equals(goal.getActive()))
                .count()
        );
        statistics.put(
            "completion_rate",
            completedGoals.size() > 0
                ? ((double) completedGoals.size() /
                    goals.values().stream().filter(goal -> Objects.equals(goal.getUserId(), userId)).count()) *
                100
                : 0.0
        );

        history.put("statistics", statistics);

        // Get total points
        int totalPoints = userPoints.getOrDefault(userId, 0);
        history.put("total_points", totalPoints);

        return history;
    }

    /**
     * Get goal suggestions based on mood patterns.
     *
     * @return the goal suggestions
     */
    @Transactional(readOnly = true)
    public List<String> getGoalSuggestions() {
        LOG.debug("Request to get goal suggestions");

        List<String> suggestions = new ArrayList<>();

        // Analyze mood patterns for the last 30 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MoodEntry> moodEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .filter(entry -> !entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(endDate))
            .collect(Collectors.toList());

        // Check for anxious patterns on specific days
        Map<java.time.DayOfWeek, Long> anxiousByDay = moodEntries
            .stream()
            .filter(entry -> entry.getMood() == MoodType.ANXIOUS)
            .collect(Collectors.groupingBy(entry -> entry.getDate().getDayOfWeek(), Collectors.counting()));

        anxiousByDay
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() >= 2)
            .forEach(entry -> {
                suggestions.add("Reduce anxious days on " + entry.getKey().toString().toLowerCase() + "s");
            });

        // Check overall mood balance
        long happyCount = moodEntries.stream().filter(e -> e.getMood() == MoodType.HAPPY).count();
        long sadCount = moodEntries.stream().filter(e -> e.getMood() == MoodType.SAD).count();

        if (sadCount > happyCount) {
            suggestions.add("Increase happy days to improve overall mood balance");
        }

        // Add general suggestions
        suggestions.add("Maintain a 7-day happy streak");
        suggestions.add("Log your mood consistently for 30 days");
        suggestions.add("Reduce anxious days by 50% this month");

        return suggestions;
    }

    /**
     * Update a mood goal.
     *
     * @param goalDTO the updated goal
     * @return true if updated successfully
     */
    public boolean updateMoodGoal(MoodGoalDTO goalDTO) {
        LOG.debug("Request to update MoodGoal : {}", goalDTO);

        if (goalDTO.getId() != null && goals.containsKey(goalDTO.getId())) {
            MoodGoalDTO existingGoal = goals.get(goalDTO.getId());

            // Update fields
            if (goalDTO.getTarget() != null) {
                existingGoal.setTarget(goalDTO.getTarget());
            }
            if (goalDTO.getDescription() != null) {
                existingGoal.setDescription(goalDTO.getDescription());
            }
            if (goalDTO.getActive() != null) {
                existingGoal.setActive(goalDTO.getActive());
            }

            goals.put(goalDTO.getId(), existingGoal);
            return true;
        }

        return false;
    }

    /**
     * Generate share content for achievements.
     *
     * @return the share content
     */
    @Transactional(readOnly = true)
    public String generateShareContent() {
        LOG.debug("Request to generate share content");

        GoalProgressDTO progress = getGoalProgress();
        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdFromLogin(currentUserLogin);
        int totalPoints = userPoints.getOrDefault(userId, 0);

        return String.format(
            "I just achieved my mood goal in Daily Mood Tracker! " +
            "Completed %d/%d %s. Total achievement points: %d. " +
            "Track your mood and set goals too!",
            progress.getCompleted(),
            progress.getTarget(),
            progress.getGoalType(),
            totalPoints
        );
    }

    /**
     * Handle streak break.
     *
     * @return the encouragement message
     */
    @Transactional(readOnly = true)
    public String handleStreakBreak() {
        LOG.debug("Request to handle streak break");

        return "Don't worry! Every streak starts with a single day. " + "You can start a new happy streak today!";
    }

    /**
     * Calculate happy days this week.
     */
    private int calculateHappyDaysThisWeek() {
        LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return (int) moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .filter(entry -> !entry.getDate().isBefore(startOfWeek) && !entry.getDate().isAfter(endOfWeek))
            .filter(entry -> entry.getMood() == MoodType.HAPPY)
            .count();
    }

    /**
     * Calculate current happy streak.
     */
    private int calculateCurrentHappyStreak() {
        List<MoodEntry> moodEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .sorted(Comparator.comparing(MoodEntry::getDate).reversed())
            .collect(Collectors.toList());

        int currentStreak = 0;
        for (MoodEntry entry : moodEntries) {
            if (entry.getMood() == MoodType.HAPPY) {
                currentStreak++;
            } else {
                break;
            }
        }

        return currentStreak;
    }

    /**
     * Calculate points earned for achieving a goal.
     */
    private int calculatePointsEarned(String goalType, int target) {
        switch (goalType) {
            case "HAPPY_DAYS":
                return target * 10;
            case "STREAK":
                return target * 15;
            case "CUSTOM":
                return 50;
            default:
                return 25;
        }
    }

    /**
     * Create empty progress when no goals exist.
     */
    private GoalProgressDTO createEmptyProgress() {
        GoalProgressDTO progress = new GoalProgressDTO();
        progress.setCompleted(0);
        progress.setTarget(0);
        progress.setPercentage(0);
        progress.setCurrentStreak(0);
        return progress;
    }

    /**
     * Get user ID from login (simplified implementation).
     */
    private Long getUserIdFromLogin(String login) {
        // In a real application, this would query the user repository
        // For now, we'll use a simple hash-based approach
        return (long) login.hashCode();
    }
}
