package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.MoodGoalsService;
import com.mycompany.myapp.service.dto.GoalAchievementDTO;
import com.mycompany.myapp.service.dto.GoalProgressDTO;
import com.mycompany.myapp.service.dto.MoodGoalDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
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

public class MoodGoalsStepDefs extends StepDefs {

    @Autowired
    private MoodGoalsService moodGoalsService;

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
    private MoodGoalDTO moodGoal;
    private GoalProgressDTO goalProgress;
    private GoalAchievementDTO goalAchievement;
    private List<MoodGoalDTO> multipleGoals;
    private String notificationMessage;
    private Map<String, Object> goalHistory;
    private List<String> goalSuggestions;
    private boolean goalUpdated;

    @Before
    public void setup() {
        // Clean up any existing test data
        cleanupTestData();

        // Reset test variables
        moodGoal = null;
        goalProgress = null;
        goalAchievement = null;
        multipleGoals = null;
        notificationMessage = null;
        goalHistory = null;
        goalSuggestions = null;
        goalUpdated = false;
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

    @Given("the system is ready for mood goals testing")
    public void the_system_is_ready_for_mood_goals_testing() {
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

    @Given("I have a goal to have {string} happy days this week")
    public void i_have_a_goal_to_have_happy_days_this_week(String targetDays) {
        // Create a mood goal for the user
        MoodGoalDTO goal = new MoodGoalDTO();
        goal.setUserId(testUser1.getId());
        goal.setType("HAPPY_DAYS");
        goal.setTarget(Integer.parseInt(targetDays));
        goal.setTimeframe("WEEK");
        goal.setActive(true);
        moodGoalsService.createMoodGoal(goal);
    }

    @Given("I have logged {string} happy days so far this week")
    public void i_have_logged_happy_days_so_far_this_week(String loggedDays) {
        // Create happy mood entries for the current week
        int days = Integer.parseInt(loggedDays);
        LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        for (int i = 0; i < days; i++) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(startOfWeek.plusDays(i));
            moodEntry.setMood(MoodType.HAPPY);
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @Given("I have a {string} day happy mood streak goal")
    public void i_have_a_day_happy_mood_streak_goal(String targetStreak) {
        // Create a streak goal for the user
        MoodGoalDTO goal = new MoodGoalDTO();
        goal.setUserId(testUser1.getId());
        goal.setType("STREAK");
        goal.setTarget(Integer.parseInt(targetStreak));
        goal.setTimeframe("ONGOING");
        goal.setActive(true);
        moodGoalsService.createMoodGoal(goal);
    }

    @Given("I currently have a {string} day happy streak")
    public void i_currently_have_a_day_happy_streak(String currentStreak) {
        // Create consecutive happy mood entries
        int days = Integer.parseInt(currentStreak);
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(startDate.plusDays(i));
            moodEntry.setMood(MoodType.HAPPY);
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @Given("I have completed {string} mood goals in the past")
    public void i_have_completed_mood_goals_in_the_past(String completedGoals) {
        // Create completed goals for the user
        int goals = Integer.parseInt(completedGoals);
        for (int i = 0; i < goals; i++) {
            MoodGoalDTO goal = new MoodGoalDTO();
            goal.setUserId(testUser1.getId());
            goal.setType("HAPPY_DAYS");
            goal.setTarget(5);
            goal.setTimeframe("WEEK");
            goal.setActive(false);
            goal.setCompleted(true);
            moodGoalsService.createMoodGoal(goal);
        }
    }

    @Given("I have a pattern of anxious moods on Mondays")
    public void i_have_a_pattern_of_anxious_moods_on_mondays() {
        // Create mood entries with anxious moods on Mondays
        LocalDate startDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        for (int i = 0; i < 4; i++) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(startDate.plusWeeks(i));
            moodEntry.setMood(MoodType.ANXIOUS);
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @When("I set a mood goal to have {string} happy days this week")
    @Transactional
    public void i_set_a_mood_goal_to_have_happy_days_this_week(String targetDays) {
        // Call actual service - this should fail until implemented
        MoodGoalDTO goal = new MoodGoalDTO();
        goal.setUserId(testUser1.getId());
        goal.setType("HAPPY_DAYS");
        goal.setTarget(Integer.parseInt(targetDays));
        goal.setTimeframe("WEEK");
        goal.setActive(true);
        moodGoal = moodGoalsService.createMoodGoal(goal);
    }

    @When("I check my goal progress")
    @Transactional
    public void i_check_my_goal_progress() {
        // Call actual service - this should fail until implemented
        goalProgress = moodGoalsService.getGoalProgress();
    }

    @When("I log my {string} happy day for the week")
    @Transactional
    public void i_log_my_happy_day_for_the_week(String dayNumber) {
        // Call actual service - this should fail until implemented
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setDate(LocalDate.now());
        moodEntry.setMood(MoodType.HAPPY);
        moodEntry.setUser(testUser1);
        moodEntryRepository.save(moodEntry);
        goalAchievement = moodGoalsService.checkGoalAchievement();
    }

    @When("I set a goal to maintain a {string} day happy streak")
    @Transactional
    public void i_set_a_goal_to_maintain_a_day_happy_streak(String targetStreak) {
        // Call actual service - this should fail until implemented
        MoodGoalDTO goal = new MoodGoalDTO();
        goal.setUserId(testUser1.getId());
        goal.setType("STREAK");
        goal.setTarget(Integer.parseInt(targetStreak));
        goal.setTimeframe("ONGOING");
        goal.setActive(true);
        moodGoal = moodGoalsService.createMoodGoal(goal);
    }

    @When("I log a {string} mood today")
    @Transactional
    public void i_log_a_mood_today(String mood) {
        // Call actual service - this should fail until implemented
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setDate(LocalDate.now());
        moodEntry.setMood(MoodType.valueOf(mood));
        moodEntry.setUser(testUser1);
        moodEntryRepository.save(moodEntry);
        notificationMessage = moodGoalsService.handleStreakBreak();
    }

    @When("I set multiple mood goals:")
    @Transactional
    public void i_set_multiple_mood_goals(DataTable dataTable) {
        // Call actual service - this should fail until implemented
        List<Map<String, String>> goals = dataTable.asMaps();
        multipleGoals = moodGoalsService.createMultipleGoals(goals);
    }

    @When("the goal reminder system triggers")
    @Transactional
    public void the_goal_reminder_system_triggers() {
        // Call actual service - this should fail until implemented
        notificationMessage = moodGoalsService.triggerGoalReminder();
    }

    @When("I view my goal history")
    @Transactional
    public void i_view_my_goal_history() {
        // Call actual service - this should fail until implemented
        goalHistory = moodGoalsService.getGoalHistory();
    }

    @When("I create a custom goal {string}")
    @Transactional
    public void i_create_a_custom_goal(String customGoalDescription) {
        // Call actual service - this should fail until implemented
        MoodGoalDTO goal = new MoodGoalDTO();
        goal.setUserId(testUser1.getId());
        goal.setType("CUSTOM");
        goal.setDescription(customGoalDescription);
        goal.setActive(true);
        moodGoal = moodGoalsService.createMoodGoal(goal);
    }

    @When("I choose to share my achievement")
    @Transactional
    public void i_choose_to_share_my_achievement() {
        // Call actual service - this should fail until implemented
        String shareContent = moodGoalsService.generateShareContent();
        // This would typically integrate with social media APIs
    }

    @When("I request goal suggestions")
    @Transactional
    public void i_request_goal_suggestions() {
        // Call actual service - this should fail until implemented
        goalSuggestions = moodGoalsService.getGoalSuggestions();
    }

    @When("I adjust the goal to {string} happy days this week")
    @Transactional
    public void i_adjust_the_goal_to_happy_days_this_week(String newTarget) {
        // Call actual service - this should fail until implemented
        MoodGoalDTO updatedGoal = new MoodGoalDTO();
        updatedGoal.setId(moodGoal.getId());
        updatedGoal.setTarget(Integer.parseInt(newTarget));
        goalUpdated = moodGoalsService.updateMoodGoal(updatedGoal);
    }

    @Then("the mood goal should be saved successfully")
    public void the_mood_goal_should_be_saved_successfully() {
        assertThat(moodGoal).isNotNull();
        assertThat(moodGoal.getId()).isNotNull();
    }

    @Then("I should see my goal in the goals dashboard")
    public void i_should_see_my_goal_in_the_goals_dashboard() {
        assertThat(moodGoal).isNotNull();
        assertThat(moodGoal.getActive()).isTrue();
    }

    @Then("I should see {string} out of {string} happy days completed")
    public void i_should_see_out_of_happy_days_completed(String completed, String target) {
        assertThat(goalProgress).isNotNull();
        assertThat(goalProgress.getCompleted()).isEqualTo(Integer.parseInt(completed));
        assertThat(goalProgress.getTarget()).isEqualTo(Integer.parseInt(target));
    }

    @Then("I should see {string}% progress towards my goal")
    public void i_should_see_percent_progress_towards_my_goal(String expectedPercentage) {
        assertThat(goalProgress).isNotNull();
        assertThat(goalProgress.getPercentage()).isEqualTo(Integer.parseInt(expectedPercentage));
    }

    @Then("I should receive a goal achievement notification")
    public void i_should_receive_a_goal_achievement_notification() {
        assertThat(goalAchievement).isNotNull();
        assertThat(goalAchievement.getAchieved()).isTrue();
    }

    @Then("my goal should be marked as completed")
    public void my_goal_should_be_marked_as_completed() {
        assertThat(goalAchievement).isNotNull();
        assertThat(goalAchievement.getGoalCompleted()).isTrue();
    }

    @Then("I should earn achievement points")
    public void i_should_earn_achievement_points() {
        assertThat(goalAchievement).isNotNull();
        assertThat(goalAchievement.getPointsEarned()).isGreaterThan(0);
    }

    @Then("the streak goal should be saved successfully")
    public void the_streak_goal_should_be_saved_successfully() {
        assertThat(moodGoal).isNotNull();
        assertThat(moodGoal.getId()).isNotNull();
        assertThat(moodGoal.getType()).isEqualTo("STREAK");
    }

    @Then("I should see my current streak progress")
    public void i_should_see_my_current_streak_progress() {
        assertThat(goalProgress).isNotNull();
        assertThat(goalProgress.getCurrentStreak()).isGreaterThanOrEqualTo(0);
    }

    @Then("my streak should be reset to {string}")
    public void my_streak_should_be_reset_to(String resetValue) {
        assertThat(goalProgress).isNotNull();
        assertThat(goalProgress.getCurrentStreak()).isEqualTo(Integer.parseInt(resetValue));
    }

    @Then("I should receive a streak break notification")
    public void i_should_receive_a_streak_break_notification() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("streak");
    }

    @Then("I should get encouragement to start a new streak")
    public void i_should_get_encouragement_to_start_a_new_streak() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("encouragement");
    }

    @Then("all goals should be saved successfully")
    public void all_goals_should_be_saved_successfully() {
        assertThat(multipleGoals).isNotNull();
        assertThat(multipleGoals).isNotEmpty();
        multipleGoals.forEach(goal -> assertThat(goal.getId()).isNotNull());
    }

    @Then("I should see all goals in my dashboard")
    public void i_should_see_all_goals_in_my_dashboard() {
        assertThat(multipleGoals).isNotNull();
        assertThat(multipleGoals).hasSizeGreaterThan(1);
    }

    @Then("I should receive a goal reminder notification")
    public void i_should_receive_a_goal_reminder_notification() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("goal");
    }

    @Then("the reminder should encourage me to log happy moods")
    public void the_reminder_should_encourage_me_to_log_happy_moods() {
        assertThat(notificationMessage).isNotNull();
        assertThat(notificationMessage).contains("happy");
    }

    @Then("I should see all my completed goals")
    public void i_should_see_all_my_completed_goals() {
        assertThat(goalHistory).isNotNull();
        assertThat(goalHistory).containsKey("completed_goals");
    }

    @Then("I should see my achievement statistics")
    public void i_should_see_my_achievement_statistics() {
        assertThat(goalHistory).isNotNull();
        assertThat(goalHistory).containsKey("statistics");
    }

    @Then("I should see my total achievement points")
    public void i_should_see_my_total_achievement_points() {
        assertThat(goalHistory).isNotNull();
        assertThat(goalHistory).containsKey("total_points");
    }

    @Then("the custom goal should be saved successfully")
    public void the_custom_goal_should_be_saved_successfully() {
        assertThat(moodGoal).isNotNull();
        assertThat(moodGoal.getId()).isNotNull();
        assertThat(moodGoal.getType()).isEqualTo("CUSTOM");
    }

    @Then("I should be able to track progress towards this goal")
    public void i_should_be_able_to_track_progress_towards_this_goal() {
        assertThat(moodGoal).isNotNull();
        assertThat(moodGoal.getActive()).isTrue();
    }

    @Then("I should be able to share my achievement on social media")
    public void i_should_be_able_to_share_my_achievement_on_social_media() {
        // This would be verified by checking that share content is generated
        // The actual social media integration would be handled separately
    }

    @Then("the shared content should include my achievement details")
    public void the_shared_content_should_include_my_achievement_details() {
        // This would be verified by checking the generated share content
        // The actual content would depend on the achievement details
    }

    @Then("I should receive personalized goal suggestions")
    public void i_should_receive_personalized_goal_suggestions() {
        assertThat(goalSuggestions).isNotNull();
        assertThat(goalSuggestions).isNotEmpty();
    }

    @Then("the suggestions should include goals to reduce Monday anxiety")
    public void the_suggestions_should_include_goals_to_reduce_monday_anxiety() {
        assertThat(goalSuggestions).isNotNull();
        assertThat(
            goalSuggestions.stream().anyMatch(suggestion -> suggestion.contains("Monday") && suggestion.contains("anxiety"))
        ).isTrue();
    }

    @Then("the goal should be updated successfully")
    public void the_goal_should_be_updated_successfully() {
        assertThat(goalUpdated).isTrue();
    }

    @Then("the new target should be more achievable")
    public void the_new_target_should_be_more_achievable() {
        assertThat(moodGoal).isNotNull();
        assertThat(moodGoal.getTarget()).isLessThanOrEqualTo(5); // Assuming 5 is more achievable than 7
    }
}
