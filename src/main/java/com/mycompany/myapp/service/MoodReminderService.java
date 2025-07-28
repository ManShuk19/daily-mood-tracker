package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.service.dto.MoodReminderDTO;
import com.mycompany.myapp.service.dto.ReminderPreferencesDTO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing mood reminders.
 */
@Service
@Transactional
public class MoodReminderService {

    private static final Logger LOG = LoggerFactory.getLogger(MoodReminderService.class);

    private final MoodEntryRepository moodEntryRepository;

    // In-memory storage for reminders and preferences (in a real application, this would be a database)
    private final Map<Long, MoodReminderDTO> reminders = new HashMap<>();
    private final Map<Long, ReminderPreferencesDTO> preferences = new HashMap<>();
    private final Map<Long, Set<LocalDate>> dismissedReminders = new HashMap<>();

    public MoodReminderService(MoodEntryRepository moodEntryRepository) {
        this.moodEntryRepository = moodEntryRepository;
    }

    /**
     * Create a new mood reminder.
     *
     * @param reminderDTO the reminder to create
     * @return the created reminder
     */
    public MoodReminderDTO createMoodReminder(MoodReminderDTO reminderDTO) {
        LOG.debug("Request to create MoodReminder : {}", reminderDTO);

        // Generate a unique ID
        Long reminderId = System.currentTimeMillis();
        reminderDTO.setId(reminderId);
        reminderDTO.setEnabled(true);

        reminders.put(reminderId, reminderDTO);

        return reminderDTO;
    }

    /**
     * Trigger daily reminder.
     *
     * @return the reminder message
     */
    @Transactional(readOnly = true)
    public String triggerDailyReminder() {
        LOG.debug("Request to trigger daily reminder");

        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdFromLogin(currentUserLogin);

        // Check if user has already logged mood today
        LocalDate today = LocalDate.now();
        boolean hasLoggedToday = moodEntryRepository.findByUserIsCurrentUser().stream().anyMatch(entry -> entry.getDate().equals(today));

        // Check if reminder was already dismissed today
        boolean wasDismissedToday = dismissedReminders.getOrDefault(userId, new HashSet<>()).contains(today);

        if (!hasLoggedToday && !wasDismissedToday) {
            // Check if user had a negative mood yesterday
            LocalDate yesterday = today.minusDays(1);
            Optional<MoodEntry> yesterdayEntry = moodEntryRepository
                .findByUserIsCurrentUser()
                .stream()
                .filter(entry -> entry.getDate().equals(yesterday))
                .findFirst();

            if (
                yesterdayEntry.isPresent() &&
                (yesterdayEntry.get().getMood() == MoodType.SAD || yesterdayEntry.get().getMood() == MoodType.ANXIOUS)
            ) {
                return "Time to check in with yourself! Yesterday was challenging, but today is a new day. " + "How are you feeling today?";
            } else {
                return "Time to log your mood! How are you feeling today?";
            }
        }

        return null; // No reminder needed
    }

    /**
     * Dismiss a reminder.
     *
     * @return true if dismissed successfully
     */
    public boolean dismissReminder() {
        LOG.debug("Request to dismiss reminder");

        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdFromLogin(currentUserLogin);
        LocalDate today = LocalDate.now();

        dismissedReminders.computeIfAbsent(userId, k -> new HashSet<>()).add(today);

        return true;
    }

    /**
     * Mark reminder as completed.
     *
     * @return true if marked successfully
     */
    public boolean markReminderAsCompleted() {
        LOG.debug("Request to mark reminder as completed");

        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdFromLogin(currentUserLogin);
        LocalDate today = LocalDate.now();

        // Remove from dismissed reminders since it's now completed
        dismissedReminders.getOrDefault(userId, new HashSet<>()).remove(today);

        return true;
    }

    /**
     * Update reminder preferences.
     *
     * @param preferencesDTO the preferences to update
     * @return the updated preferences
     */
    public ReminderPreferencesDTO updateReminderPreferences(ReminderPreferencesDTO preferencesDTO) {
        LOG.debug("Request to update reminder preferences : {}", preferencesDTO);

        // Generate a unique ID if not exists
        if (preferencesDTO.getId() == null) {
            Long prefId = System.currentTimeMillis();
            preferencesDTO.setId(prefId);
        }

        preferences.put(preferencesDTO.getId(), preferencesDTO);

        return preferencesDTO;
    }

    /**
     * Get reminder preferences for the current user.
     *
     * @return the reminder preferences
     */
    @Transactional(readOnly = true)
    public ReminderPreferencesDTO getReminderPreferences() {
        LOG.debug("Request to get reminder preferences");

        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = getUserIdFromLogin(currentUserLogin);

        return preferences
            .values()
            .stream()
            .filter(pref -> Objects.equals(pref.getUserId(), userId))
            .findFirst()
            .orElse(createDefaultPreferences(userId));
    }

    /**
     * Check if reminders are enabled for the current user.
     *
     * @return true if reminders are enabled
     */
    @Transactional(readOnly = true)
    public boolean areRemindersEnabled() {
        ReminderPreferencesDTO userPrefs = getReminderPreferences();
        return Boolean.TRUE.equals(userPrefs.getDailyReminderEnabled()) || Boolean.TRUE.equals(userPrefs.getWeeklySummaryEnabled());
    }

    /**
     * Get custom reminder message for the current user.
     *
     * @return the custom message or default message
     */
    @Transactional(readOnly = true)
    public String getCustomReminderMessage() {
        ReminderPreferencesDTO userPrefs = getReminderPreferences();
        return userPrefs.getCustomMessage() != null
            ? userPrefs.getCustomMessage()
            : "Time to check in with yourself! How are you feeling today?";
    }

    /**
     * Check if it's time for a weekly summary reminder.
     *
     * @return true if it's time for weekly summary
     */
    @Transactional(readOnly = true)
    public boolean isTimeForWeeklySummary() {
        ReminderPreferencesDTO userPrefs = getReminderPreferences();

        if (!Boolean.TRUE.equals(userPrefs.getWeeklySummaryEnabled())) {
            return false;
        }

        String preferredDay = userPrefs.getWeeklySummaryDay();
        if (preferredDay == null) {
            return false;
        }

        String currentDay = LocalDate.now().getDayOfWeek().toString();
        return currentDay.equalsIgnoreCase(preferredDay);
    }

    /**
     * Generate weekly summary message.
     *
     * @return the weekly summary message
     */
    @Transactional(readOnly = true)
    public String generateWeeklySummary() {
        LOG.debug("Request to generate weekly summary");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<MoodEntry> weekEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .filter(entry -> !entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(endDate))
            .collect(Collectors.toList());

        if (weekEntries.isEmpty()) {
            return "Weekly Summary: No mood entries this week. Don't forget to log your moods!";
        }

        long happyCount = weekEntries.stream().filter(e -> e.getMood() == MoodType.HAPPY).count();
        long sadCount = weekEntries.stream().filter(e -> e.getMood() == MoodType.SAD).count();
        long anxiousCount = weekEntries.stream().filter(e -> e.getMood() == MoodType.ANXIOUS).count();

        StringBuilder summary = new StringBuilder();
        summary.append("Weekly Summary: ");
        summary.append("Happy days: ").append(happyCount).append(", ");
        summary.append("Sad days: ").append(sadCount).append(", ");
        summary.append("Anxious days: ").append(anxiousCount).append(". ");

        if (happyCount > sadCount) {
            summary.append("Great week! Keep up the positive energy!");
        } else if (sadCount > happyCount) {
            summary.append("It's been a challenging week. Remember, it's okay to not be okay.");
        } else {
            summary.append("Balanced week. Keep tracking your moods!");
        }

        return summary.toString();
    }

    /**
     * Check if user has reached a streak target.
     *
     * @param target the streak target
     * @return true if target reached
     */
    @Transactional(readOnly = true)
    public boolean hasReachedStreakTarget(int target) {
        List<MoodEntry> moodEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .sorted(Comparator.comparing(MoodEntry::getDate).reversed())
            .collect(Collectors.toList());

        int currentStreak = 0;
        for (MoodEntry entry : moodEntries) {
            if (entry.getMood() == MoodType.HAPPY) {
                currentStreak++;
                if (currentStreak >= target) {
                    return true;
                }
            } else {
                break;
            }
        }

        return false;
    }

    /**
     * Get motivational message based on recent mood patterns.
     *
     * @return the motivational message
     */
    @Transactional(readOnly = true)
    public String getMotivationalMessage() {
        LOG.debug("Request to get motivational message");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<MoodEntry> recentEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .filter(entry -> !entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(endDate))
            .collect(Collectors.toList());

        if (recentEntries.isEmpty()) {
            return "Every journey begins with a single step. Start tracking your mood today!";
        }

        long happyCount = recentEntries.stream().filter(e -> e.getMood() == MoodType.HAPPY).count();
        long sadCount = recentEntries.stream().filter(e -> e.getMood() == MoodType.SAD).count();
        long anxiousCount = recentEntries.stream().filter(e -> e.getMood() == MoodType.ANXIOUS).count();

        if (sadCount > happyCount) {
            return (
                "Remember, difficult times are temporary. You're stronger than you think. " +
                "Take a moment to breathe and be kind to yourself."
            );
        } else if (anxiousCount > recentEntries.size() * 0.3) {
            return "It's normal to feel anxious. Try some deep breathing exercises or " + "take a short walk. You're doing great!";
        } else {
            return "You're making progress! Keep up the great work with your mood tracking.";
        }
    }

    /**
     * Create default preferences for a user.
     */
    private ReminderPreferencesDTO createDefaultPreferences(Long userId) {
        ReminderPreferencesDTO defaultPrefs = new ReminderPreferencesDTO();
        defaultPrefs.setId(System.currentTimeMillis());
        defaultPrefs.setUserId(userId);
        defaultPrefs.setDailyReminderEnabled(true);
        defaultPrefs.setDailyReminderTime(LocalTime.of(18, 0));
        defaultPrefs.setWeeklySummaryEnabled(true);
        defaultPrefs.setWeeklySummaryDay("SUNDAY");
        defaultPrefs.setNotificationType("in_app");
        defaultPrefs.setCustomMessage("Time to check in with yourself! How are you feeling today?");

        preferences.put(defaultPrefs.getId(), defaultPrefs);
        return defaultPrefs;
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
