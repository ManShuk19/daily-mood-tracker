package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.MoodAnalyticsService;
import com.mycompany.myapp.service.dto.MoodStatisticsDTO;
import com.mycompany.myapp.service.dto.MoodStreakDTO;
import com.mycompany.myapp.service.dto.MoodTrendDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

public class MoodAnalyticsStepDefs extends StepDefs {

    @Autowired
    private MoodAnalyticsService moodAnalyticsService;

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
    private MoodStatisticsDTO moodStatistics;
    private MoodTrendDTO moodTrend;
    private MoodStreakDTO moodStreak;
    private String exportedData;
    private Map<String, Object> moodComparison;
    private Map<String, Object> moodInsights;
    private String moodReport;

    @Before
    public void setup() {
        // Clean up any existing test data
        cleanupTestData();

        // Reset test variables
        moodStatistics = null;
        moodTrend = null;
        moodStreak = null;
        exportedData = null;
        moodComparison = null;
        moodInsights = null;
        moodReport = null;
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

    @Given("the system is ready for mood analytics testing")
    public void the_system_is_ready_for_mood_analytics_testing() {
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

    @Given("I have mood entries for the current month:")
    public void i_have_mood_entries_for_the_current_month(DataTable dataTable) {
        List<Map<String, String>> entries = dataTable.asMaps();
        for (Map<String, String> entry : entries) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(LocalDate.parse(entry.get("date")));
            moodEntry.setMood(MoodType.valueOf(entry.get("mood")));
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @Given("I have mood entries for the last 7 days:")
    public void i_have_mood_entries_for_the_last_7_days(DataTable dataTable) {
        List<Map<String, String>> entries = dataTable.asMaps();
        for (Map<String, String> entry : entries) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(LocalDate.parse(entry.get("date")));
            moodEntry.setMood(MoodType.valueOf(entry.get("mood")));
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @Given("I have consecutive mood entries:")
    public void i_have_consecutive_mood_entries(DataTable dataTable) {
        List<Map<String, String>> entries = dataTable.asMaps();
        for (Map<String, String> entry : entries) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(LocalDate.parse(entry.get("date")));
            moodEntry.setMood(MoodType.valueOf(entry.get("mood")));
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @Given("I have mood entries for the last 30 days")
    public void i_have_mood_entries_for_the_last_30_days() {
        // Create sample mood entries for the last 30 days
        LocalDate startDate = LocalDate.now().minusDays(29);
        for (int i = 0; i < 30; i++) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(startDate.plusDays(i));
            moodEntry.setMood(MoodType.values()[i % MoodType.values().length]);
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @Given("I have mood entries for different periods:")
    public void i_have_mood_entries_for_different_periods(DataTable dataTable) {
        List<Map<String, String>> entries = dataTable.asMaps();
        for (Map<String, String> entry : entries) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(LocalDate.parse(entry.get("date")));
            moodEntry.setMood(MoodType.valueOf(entry.get("mood")));
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
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

    @Given("I have comprehensive mood data for the last 3 months")
    public void i_have_comprehensive_mood_data_for_the_last_3_months() {
        // Create comprehensive mood data for the last 3 months
        LocalDate startDate = LocalDate.now().minusMonths(3);
        for (int i = 0; i < 90; i++) {
            MoodEntry moodEntry = new MoodEntry();
            moodEntry.setDate(startDate.plusDays(i));
            moodEntry.setMood(MoodType.values()[i % MoodType.values().length]);
            moodEntry.setUser(testUser1);
            moodEntryRepository.save(moodEntry);
        }
    }

    @When("I request mood statistics for {string}")
    @Transactional
    public void i_request_mood_statistics_for_month(String month) {
        // Call actual service - this should fail until implemented
        moodStatistics = moodAnalyticsService.getMoodStatisticsForMonth(month);
    }

    @When("I request mood trend for the last {string} days")
    @Transactional
    public void i_request_mood_trend_for_the_last_days(String days) {
        // Call actual service - this should fail until implemented
        moodTrend = moodAnalyticsService.getMoodTrendForLastDays(Integer.parseInt(days));
    }

    @When("I request streak information")
    @Transactional
    public void i_request_streak_information() {
        // Call actual service - this should fail until implemented
        moodStreak = moodAnalyticsService.getMoodStreakInformation();
    }

    @When("I request mood data export for {string}")
    @Transactional
    public void i_request_mood_data_export_for_month(String month) {
        // Call actual service - this should fail until implemented
        exportedData = moodAnalyticsService.exportMoodDataForMonth(month);
    }

    @When("I compare mood patterns between {string} and {string}")
    @Transactional
    public void i_compare_mood_patterns_between_periods(String period1, String period2) {
        // Call actual service - this should fail until implemented
        moodComparison = moodAnalyticsService.compareMoodPatterns(period1, period2);
    }

    @When("I request mood insights")
    @Transactional
    public void i_request_mood_insights() {
        // Call actual service - this should fail until implemented
        moodInsights = moodAnalyticsService.getMoodInsights();
    }

    @When("I generate a mood summary report for {string} to {string}")
    @Transactional
    public void i_generate_a_mood_summary_report_for_period(String startPeriod, String endPeriod) {
        // Call actual service - this should fail until implemented
        moodReport = moodAnalyticsService.generateMoodSummaryReport(startPeriod, endPeriod);
    }

    @Then("I should get mood statistics with:")
    public void i_should_get_mood_statistics_with(DataTable dataTable) {
        assertThat(moodStatistics).isNotNull();
        Map<String, String> expectedStats = dataTable.asMap();

        // Only assertions, no business logic
        assertThat(moodStatistics.getTotalEntries()).isEqualTo(Integer.parseInt(expectedStats.get("total_entries")));
        assertThat(moodStatistics.getHappyCount()).isEqualTo(Integer.parseInt(expectedStats.get("happy_count")));
        assertThat(moodStatistics.getSadCount()).isEqualTo(Integer.parseInt(expectedStats.get("sad_count")));
        assertThat(moodStatistics.getNeutralCount()).isEqualTo(Integer.parseInt(expectedStats.get("neutral_count")));
        assertThat(moodStatistics.getMostCommonMood()).isEqualTo(MoodType.valueOf(expectedStats.get("most_common_mood")));
    }

    @Then("I should get a mood trend with {string} data points")
    public void i_should_get_a_mood_trend_with_data_points(String expectedCount) {
        assertThat(moodTrend).isNotNull();
        assertThat(moodTrend.getDataPoints()).hasSize(Integer.parseInt(expectedCount));
    }

    @Then("the trend should show mood progression over time")
    public void the_trend_should_show_mood_progression_over_time() {
        assertThat(moodTrend).isNotNull();
        assertThat(moodTrend.getDataPoints()).isNotEmpty();
    }

    @Then("I should get current happy streak of {string} days")
    public void i_should_get_current_happy_streak_of_days(String expectedStreak) {
        assertThat(moodStreak).isNotNull();
        assertThat(moodStreak.getCurrentHappyStreak()).isEqualTo(Integer.parseInt(expectedStreak));
    }

    @Then("I should get longest happy streak of {string} days")
    public void i_should_get_longest_happy_streak_of_days(String expectedLongestStreak) {
        assertThat(moodStreak).isNotNull();
        assertThat(moodStreak.getLongestHappyStreak()).isEqualTo(Integer.parseInt(expectedLongestStreak));
    }

    @Then("I should receive a CSV file with mood data")
    public void i_should_receive_a_csv_file_with_mood_data() {
        assertThat(exportedData).isNotNull();
        assertThat(exportedData).contains("date,mood");
    }

    @Then("the file should contain all my mood entries for the month")
    public void the_file_should_contain_all_my_mood_entries_for_the_month() {
        assertThat(exportedData).isNotNull();
        // Additional assertions for file content would go here
    }

    @Then("I should see improved mood trend in {string}")
    public void i_should_see_improved_mood_trend_in_period(String period) {
        assertThat(moodComparison).isNotNull();
        assertThat(moodComparison).containsKey("improvement");
    }

    @Then("the comparison should show percentage changes")
    public void the_comparison_should_show_percentage_changes() {
        assertThat(moodComparison).isNotNull();
        assertThat(moodComparison).containsKey("percentage_changes");
    }

    @Then("I should receive personalized mood insights")
    public void i_should_receive_personalized_mood_insights() {
        assertThat(moodInsights).isNotNull();
        assertThat(moodInsights).containsKey("insights");
    }

    @Then("I should get recommendations for improving mood")
    public void i_should_get_recommendations_for_improving_mood() {
        assertThat(moodInsights).isNotNull();
        assertThat(moodInsights).containsKey("recommendations");
    }

    @Then("I should get a professional mood summary report")
    public void i_should_get_a_professional_mood_summary_report() {
        assertThat(moodReport).isNotNull();
        assertThat(moodReport).contains("Mood Summary Report");
    }

    @Then("the report should include mood trends and patterns")
    public void the_report_should_include_mood_trends_and_patterns() {
        assertThat(moodReport).isNotNull();
        assertThat(moodReport).contains("trends");
        assertThat(moodReport).contains("patterns");
    }
}
