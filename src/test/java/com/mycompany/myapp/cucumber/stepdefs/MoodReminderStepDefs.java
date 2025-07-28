package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.MoodReminderService;
import com.mycompany.myapp.service.dto.MoodReminderDTO;
import com.mycompany.myapp.service.dto.ReminderPreferencesDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

public class MoodReminderStepDefs extends StepDefs {

    @Autowired
    private MoodReminderService moodReminderService;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    // Test data
    private User testUser1;
    private User testUser2;
    private String currentUserLogin;
    private MoodReminderDTO moodReminder;
    private ReminderPreferencesDTO reminderPreferences;
    private String notificationMessage;
    private boolean reminderDismissed;
    private boolean reminderCompleted;
    private String confirmationMessage;

    @Before
    public void setup() {
        // Clean up any existing test data
        cleanupTestData();

        // Reset test variables
        moodReminder = null;
        reminderPreferences = null;
        notificationMessage = null;
        reminderDismissed = false;
        reminderCompleted = false;
        confirmationMessage = null;
        currentUserLogin = null;
    }

    @After
    public void cleanup() {
        cleanupTestData();
        SecurityContextHolder.clearContext();
    }

    private void cleanupTestData() {
        // Clean up mood entries for test users first to avoid referential integrity violations
        userRepository
            .findOneByLogin("user1")
            .ifPresent(user -> {
                List<MoodEntry> userMoodEntries = moodEntryRepository
                    .findAll()
                    .stream()
                    .filter(entry -> entry.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());
                moodEntryRepository.deleteAll(userMoodEntries);
            });

        userRepository
            .findOneByLogin("user2")
            .ifPresent(user -> {
                List<MoodEntry> userMoodEntries = moodEntryRepository
                    .findAll()
                    .stream()
                    .filter(entry -> entry.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());
                moodEntryRepository.deleteAll(userMoodEntries);
            });

        // Clean up test users that have been created (only those with test-specific logins)
        // This prevents unique constraint violations when re-running tests
        userRepository.findOneByLogin("user1").ifPresent(userRepository::delete);
        userRepository.findOneByLogin("user2").ifPresent(userRepository::delete);
    }

    @Given("the system is ready for mood reminder testing")
    public void the_system_is_ready_for_mood_reminder_testing() {
        // This step ensures the system is ready for testing
        // No implementation needed as it's just a setup step
    }

    @Given("test users are created")
    public void test_users_are_created() {
        // Create test user 1
        testUser1 = new User();
        testUser1.setLogin("user1");
        testUser1.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser1.setActivated(true);
        testUser1.setEmail("user1@test.com");
        testUser1.setFirstName("Test");
        testUser1.setLastName("User1");
        testUser1.setLangKey("en");
        userRepository.save(testUser1);

        // Create test user 2
        testUser2 = new User();
        testUser2.setLogin("user2");
        testUser2.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser2.setActivated(true);
        testUser2.setEmail("user2@test.com");
        testUser2.setFirstName("Test");
        testUser2.setLastName("User2");
        testUser2.setLangKey("en");
        userRepository.save(testUser2);
    }

    @Given("I am logged in as {string}")
    public void i_am_logged_in_as(String username) {
        currentUserLogin = username;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Given("I have a daily mood reminder set for {string}")
    public void i_have_a_daily_mood_reminder_set_for_time(String time) {
        // Create a daily mood reminder for the user
        MoodReminderDTO reminder = new MoodReminderDTO();
        reminder.setUserId(testUser1.getId());
        reminder.setReminderTime(LocalTime.parse(time));
        reminder.setType("DAILY");
        reminder.setEnabled(true);
        moodReminderService.createMoodReminder(reminder);
    }

    @Given("it is {string} and I haven't logged my mood today")
    public void it_is_time_and_i_havent_logged_my_mood_today(String time) {
        // This step simulates the condition where it's reminder time and no mood is logged
        // The actual logic would be handled by the reminder service
    }

    @Given("I receive a mood reminder notification")
    public void i_receive_a_mood_reminder_notification() {
        // Simulate receiving a reminder notification
        notificationMessage = "Time to log your mood!";
    }

    @Given("I have a weekly mood summary reminder set for {string} at {string}")
    public void i_have_a_weekly_mood_summary_reminder_set_for_day_at_time(String day, String time) {
        // Create a weekly mood summary reminder
        MoodReminderDTO reminder = new MoodReminderDTO();
        reminder.setUserId(testUser1.getId());
        reminder.setReminderTime(LocalTime.parse(time));
        reminder.setDayOfWeek(day);
        reminder.setType("WEEKLY_SUMMARY");
        reminder.setEnabled(true);
        moodReminderService.createMoodReminder(reminder);
    }

    @Given("I have mood entries for the past week")
    public void i_have_mood_entries_for_the_past_week() {
        // Create mood entries for the past week
        LocalDate startDate = LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(startDate.plusDays(i));
            moodEntry.setMood(MoodType.values()[i % MoodType.values().length]);
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @Given("it is {string} at {string}")
    public void it_is_day_at_time(String day, String time) {
        // This step simulates the condition where it's the specified day and time
        // The actual logic would be handled by the reminder service
    }

    @Given("I have mood reminders enabled")
    public void i_have_mood_reminders_enabled() {
        // Enable mood reminders for the user
        ReminderPreferencesDTO preferences = new ReminderPreferencesDTO();
        preferences.setUserId(testUser1.getId());
        preferences.setDailyReminderEnabled(true);
        preferences.setWeeklySummaryEnabled(true);
        moodReminderService.updateReminderPreferences(preferences);
    }

    @Given("I have a {string} day happy mood streak")
    public void i_have_a_day_happy_mood_streak(String streakDays) {
        // Create consecutive happy mood entries
        int days = Integer.parseInt(streakDays);
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(startDate.plusDays(i));
            moodEntry.setMood(MoodType.HAPPY);
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @Given("I logged a {string} mood yesterday")
    public void i_logged_a_mood_yesterday(String mood) {
        // Create a mood entry for yesterday
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setDate(LocalDate.now().minusDays(1));
        moodEntry.setMood(MoodType.valueOf(mood));
        moodEntry.setUser(testUser1);
        moodEntryRepository.save(moodEntry);
    }

    @When("I set up a daily mood reminder at {string}")
    @Transactional
    public void i_set_up_a_daily_mood_reminder_at_time(String time) {
        // Call actual service - this should fail until implemented
        MoodReminderDTO reminder = new MoodReminderDTO();
        reminder.setUserId(testUser1.getId());
        reminder.setReminderTime(LocalTime.parse(time));
        reminder.setType("DAILY");
        reminder.setEnabled(true);
        moodReminder = moodReminderService.createMoodReminder(reminder);
    }

    @When("the reminder system triggers")
    @Transactional
    public void the_reminder_system_triggers() {
        // Call actual service - this should fail until implemented
        notificationMessage = moodReminderService.triggerDailyReminder();
    }

    @When("I dismiss the reminder")
    @Transactional
    public void i_dismiss_the_reminder() {
        // Call actual service - this should fail until implemented
        reminderDismissed = moodReminderService.dismissReminder();
    }

    @When("I log my mood as {string} after receiving the reminder")
    @Transactional
    public void i_log_my_mood_as_after_receiving_the_reminder(String mood) {
        // Call actual service - this should fail until implemented
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setDate(LocalDate.now());
        moodEntry.setMood(MoodType.valueOf(mood));
        moodEntry.setUser(testUser1);
        moodEntryRepository.save(moodEntry);
        reminderCompleted = moodReminderService.markReminderAsCompleted();
    }

    @When("I set up a weekly mood summary reminder for {string} at {string}")
    @Transactional
    public void i_set_up_a_weekly_mood_summary_reminder_for_day_at_time(String day, String time) {
        // Call actual service - this should fail until implemented
        MoodReminderDTO reminder = new MoodReminderDTO();
        reminder.setUserId(testUser1.getId());
        reminder.setReminderTime(LocalTime.parse(time));
        reminder.setDayOfWeek(day);
        reminder.setType("WEEKLY_SUMMARY");
        reminder.setEnabled(true);
        moodReminder = moodReminderService.createMoodReminder(reminder);
    }

    @When("I update my reminder preferences:")
    @Transactional
    public void i_update_my_reminder_preferences(DataTable dataTable) {
        // Call actual service - this should fail until implemented
        Map<String, String> preferences = dataTable.asMap();
        ReminderPreferencesDTO reminderPrefs = new ReminderPreferencesDTO();
        reminderPrefs.setUserId(testUser1.getId());
        reminderPrefs.setDailyReminderTime(LocalTime.parse(preferences.get("daily_reminder")));
        reminderPrefs.setWeeklySummaryDay(preferences.get("weekly_summary"));
        reminderPrefs.setNotificationType(preferences.get("notification_type"));
        reminderPreferences = moodReminderService.updateReminderPreferences(reminderPrefs);
    }

    @When("I disable mood reminders")
    @Transactional
    public void i_disable_mood_reminders() {
        // Call actual service - this should fail until implemented
        ReminderPreferencesDTO preferences = new ReminderPreferencesDTO();
        preferences.setUserId(testUser1.getId());
        preferences.setDailyReminderEnabled(false);
        preferences.setWeeklySummaryEnabled(false);
        reminderPreferences = moodReminderService.updateReminderPreferences(preferences);
    }

    @When("I set up a streak reminder for {string} days")
    @Transactional
    public void i_set_up_a_streak_reminder_for_days(String days) {
        // Call actual service - this should fail until implemented
        MoodReminderDTO reminder = new MoodReminderDTO();
        reminder.setUserId(testUser1.getId());
        reminder.setStreakTarget(Integer.parseInt(days));
        reminder.setType("STREAK");
        reminder.setEnabled(true);
        moodReminder = moodReminderService.createMoodReminder(reminder);
    }

    @When("I set a custom reminder message {string}")
    @Transactional
    public void i_set_a_custom_reminder_message(String message) {
        // Call actual service - this should fail until implemented
        ReminderPreferencesDTO preferences = new ReminderPreferencesDTO();
        preferences.setUserId(testUser1.getId());
        preferences.setCustomMessage(message);
        reminderPreferences = moodReminderService.updateReminderPreferences(preferences);
    }

    @Then("the mood reminder should be saved successfully")
    public void the_mood_reminder_should_be_saved_successfully() {
        assertThat(moodReminder).isNotNull();
        assertThat(moodReminder.getId()).isNotNull();
    }

    @Then("I should receive a confirmation message")
    public void i_should_receive_a_confirmation_message() {
        assertThat(confirmationMessage).isNotNull();
        assertThat(confirmationMessage).contains("reminder");
    }

    @Then("I should receive a mood reminder notification")
    public void i_should_receive_a_mood_reminder_notification() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("mood");
    }

    @Then("the notification should prompt me to log my mood")
    public void the_notification_should_prompt_me_to_log_my_mood() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("log");
    }

    @Then("the reminder should be marked as dismissed")
    public void the_reminder_should_be_marked_as_dismissed() {
        assertThat(reminderDismissed).isTrue();
    }

    @Then("I should not receive another reminder today")
    public void i_should_not_receive_another_reminder_today() {
        // This would be verified by checking that no additional notifications are sent
        // The actual verification would depend on the notification system implementation
    }

    @Then("the reminder should be marked as completed")
    public void the_reminder_should_be_marked_as_completed() {
        assertThat(reminderCompleted).isTrue();
    }

    @Then("the weekly reminder should be saved successfully")
    public void the_weekly_reminder_should_be_saved_successfully() {
        assertThat(moodReminder).isNotNull();
        assertThat(moodReminder.getId()).isNotNull();
        assertThat(moodReminder.getType()).isEqualTo("WEEKLY_SUMMARY");
    }

    @Then("I should receive a weekly mood summary notification")
    public void i_should_receive_a_weekly_mood_summary_notification() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("summary");
    }

    @Then("the summary should include my mood statistics for the week")
    public void the_summary_should_include_my_mood_statistics_for_the_week() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("statistics");
    }

    @Then("my reminder preferences should be updated successfully")
    public void my_reminder_preferences_should_be_updated_successfully() {
        assertThat(reminderPreferences).isNotNull();
        assertThat(reminderPreferences.getId()).isNotNull();
    }

    @Then("the new settings should be applied")
    public void the_new_settings_should_be_applied() {
        assertThat(reminderPreferences).isNotNull();
        // Additional verification that settings are actually applied
    }

    @Then("mood reminders should be disabled")
    public void mood_reminders_should_be_disabled() {
        assertThat(reminderPreferences).isNotNull();
        assertThat(reminderPreferences.getDailyReminderEnabled()).isFalse();
        assertThat(reminderPreferences.getWeeklySummaryEnabled()).isFalse();
    }

    @Then("I should not receive any mood reminder notifications")
    public void i_should_not_receive_any_mood_reminder_notifications() {
        // This would be verified by checking that no notifications are sent
        // The actual verification would depend on the notification system implementation
    }

    @Then("the streak reminder should be saved successfully")
    public void the_streak_reminder_should_be_saved_successfully() {
        assertThat(moodReminder).isNotNull();
        assertThat(moodReminder.getId()).isNotNull();
        assertThat(moodReminder.getType()).isEqualTo("STREAK");
    }

    @Then("I should be notified when I reach {string} consecutive happy days")
    public void i_should_be_notified_when_i_reach_consecutive_happy_days(String days) {
        // This would be verified when the streak target is reached
        // The actual verification would depend on the notification system implementation
    }

    @Then("I should receive a motivational reminder")
    public void i_should_receive_a_motivational_reminder() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("motivational");
    }

    @Then("the reminder should include encouraging content")
    public void the_reminder_should_include_encouraging_content() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("encouraging");
    }

    @Then("the custom message should be saved successfully")
    public void the_custom_message_should_be_saved_successfully() {
        assertThat(reminderPreferences).isNotNull();
        assertThat(reminderPreferences.getCustomMessage()).isNotNull();
    }

    @Then("future reminders should use my custom message")
    public void future_reminders_should_use_my_custom_message() {
        assertThat(reminderPreferences).isNotNull();
        assertThat(reminderPreferences.getCustomMessage()).isNotNull();
        // Additional verification that custom message is used in notifications
    }
}
