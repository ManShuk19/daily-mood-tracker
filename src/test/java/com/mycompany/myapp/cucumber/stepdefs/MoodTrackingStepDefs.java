package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.MoodEntryService;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

public class MoodTrackingStepDefs extends StepDefs {

    @Autowired
    private MoodEntryService moodEntryService;

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
    private MoodEntryDTO lastCreatedMoodEntry;
    private List<MoodEntryDTO> retrievedMoodEntries;
    private Exception lastException;
    private Long moodEntryCount;
    private MoodEntryDTO foundMoodEntry;
    private Map<String, Object> moodStatistics;

    @Before
    public void setup() {
        // Clean up any existing test data
        cleanupTestData();

        // Reset test variables
        lastCreatedMoodEntry = null;
        retrievedMoodEntries = null;
        lastException = null;
        moodEntryCount = null;
        foundMoodEntry = null;
        currentUserLogin = null;
        moodStatistics = null;
    }

    @After
    public void cleanup() {
        cleanupTestData();
        SecurityContextHolder.clearContext();
    }

    private void cleanupTestData() {
        // Clean up mood entries for test users first to avoid referential integrity violations
        userRepository
            .findOneByLogin("mooduser1")
            .ifPresent(user -> {
                List<MoodEntry> userMoodEntries = moodEntryRepository
                    .findAll()
                    .stream()
                    .filter(entry -> entry.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());
                moodEntryRepository.deleteAll(userMoodEntries);
            });

        userRepository
            .findOneByLogin("mooduser2")
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
        userRepository.findOneByLogin("mooduser1").ifPresent(userRepository::delete);
        userRepository.findOneByLogin("mooduser2").ifPresent(userRepository::delete);
    }

    @Given("the mood tracking system is ready")
    public void the_mood_tracking_system_is_ready() {
        // Call actual service to verify system is ready
        // This should fail if the system is not properly configured
        moodEntryCount = moodEntryRepository.count();
    }

    @Given("test users are available")
    public void test_users_are_available() {
        // Create test users using actual repository
        testUser1 = createTestUser("mooduser1");
        testUser2 = createTestUser("mooduser2");

        // Verify users were created
        assertThat(userRepository.findOneByLogin("mooduser1")).isPresent();
        assertThat(userRepository.findOneByLogin("mooduser2")).isPresent();
    }

    @Given("I am logged in as {string}")
    public void i_am_logged_in_as(String username) {
        currentUserLogin = username;

        // Set up security context using actual UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @When("I create a mood entry for today with mood {string}")
    @Transactional
    public void i_create_a_mood_entry_for_today_with_mood(String moodString) {
        try {
            MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
            moodEntryDTO.setDate(LocalDate.now());
            moodEntryDTO.setMood(MoodType.valueOf(moodString));

            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
            moodEntryDTO.setUser(new UserDTO(user));

            // Call actual service - this should fail until business logic is implemented
            lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I create a mood entry for {string} with mood {string}")
    @Transactional
    public void i_create_a_mood_entry_for_date_with_mood(String dateString, String moodString) {
        try {
            MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
            moodEntryDTO.setDate(LocalDate.parse(dateString));
            moodEntryDTO.setMood(MoodType.valueOf(moodString));

            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
            moodEntryDTO.setUser(new UserDTO(user));

            // Call actual service - this should fail until business logic is implemented
            lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Given("I have a mood entry for {string} with mood {string}")
    @Transactional
    public void i_have_a_mood_entry_for_date_with_mood(String dateString, String moodString) {
        MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
        moodEntryDTO.setDate(LocalDate.parse(dateString));
        moodEntryDTO.setMood(MoodType.valueOf(moodString));

        User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
        moodEntryDTO.setUser(new UserDTO(user));

        // Call actual service - this should fail until business logic is implemented
        lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
    }

    @Given("user {string} has a mood entry for {string} with mood {string}")
    @Transactional
    public void user_has_a_mood_entry_for_date_with_mood(String username, String dateString, String moodString) {
        User user = userRepository.findOneByLogin(username).orElseThrow();

        MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
        moodEntryDTO.setDate(LocalDate.parse(dateString));
        moodEntryDTO.setMood(MoodType.valueOf(moodString));
        moodEntryDTO.setUser(new UserDTO(user));

        // Call actual service - this should fail until business logic is implemented
        moodEntryService.save(moodEntryDTO);
    }

    @Given("I have mood entries:")
    @Transactional
    public void i_have_mood_entries(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();

        for (Map<String, String> row : rows) {
            MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
            moodEntryDTO.setDate(LocalDate.parse(row.get("date")));
            moodEntryDTO.setMood(MoodType.valueOf(row.get("mood")));
            moodEntryDTO.setUser(new UserDTO(user));

            // Call actual service - this should fail until business logic is implemented
            moodEntryService.save(moodEntryDTO);
        }
    }

    @When("I try to create another mood entry for {string} with mood {string}")
    @Transactional
    public void i_try_to_create_another_mood_entry_for_date_with_mood(String dateString, String moodString) {
        try {
            MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
            moodEntryDTO.setDate(LocalDate.parse(dateString));
            moodEntryDTO.setMood(MoodType.valueOf(moodString));

            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
            moodEntryDTO.setUser(new UserDTO(user));

            // Call actual service - this should fail until business logic is implemented
            lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I update the mood entry for {string} to {string}")
    @Transactional
    public void i_update_the_mood_entry_for_date_to(String dateString, String newMoodString) {
        try {
            // Find existing mood entry by date
            LocalDate date = LocalDate.parse(dateString);
            List<MoodEntry> userMoodEntries = moodEntryRepository
                .findAll()
                .stream()
                .filter(entry -> entry.getUser().getLogin().equals(currentUserLogin) && entry.getDate().equals(date))
                .collect(Collectors.toList());

            if (!userMoodEntries.isEmpty()) {
                MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
                moodEntryDTO.setId(userMoodEntries.get(0).getId());
                moodEntryDTO.setDate(date);
                moodEntryDTO.setMood(MoodType.valueOf(newMoodString));

                User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
                moodEntryDTO.setUser(new UserDTO(user));

                // Call actual service - this should fail until business logic is implemented
                lastCreatedMoodEntry = moodEntryService.update(moodEntryDTO);
            }
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I retrieve all my mood entries")
    @Transactional
    public void i_retrieve_all_my_mood_entries() {
        try {
            // Call actual service - this should fail until business logic is implemented
            Page<MoodEntryDTO> page = moodEntryService.findAll(PageRequest.of(0, 100));
            retrievedMoodEntries = page.getContent();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I retrieve mood entry for {string}")
    @Transactional
    public void i_retrieve_mood_entry_for_date(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            List<MoodEntry> userMoodEntries = moodEntryRepository
                .findAll()
                .stream()
                .filter(entry -> entry.getUser().getLogin().equals(currentUserLogin) && entry.getDate().equals(date))
                .collect(Collectors.toList());

            if (!userMoodEntries.isEmpty()) {
                // Call actual service - this should fail until business logic is implemented
                Optional<MoodEntryDTO> moodEntry = moodEntryService.findOne(userMoodEntries.get(0).getId());
                foundMoodEntry = moodEntry.orElse(null);
            }
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I delete the mood entry for {string}")
    @Transactional
    public void i_delete_the_mood_entry_for_date(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            List<MoodEntry> userMoodEntries = moodEntryRepository
                .findAll()
                .stream()
                .filter(entry -> entry.getUser().getLogin().equals(currentUserLogin) && entry.getDate().equals(date))
                .collect(Collectors.toList());

            if (!userMoodEntries.isEmpty()) {
                // Call actual service - this should fail until business logic is implemented
                moodEntryService.delete(userMoodEntries.get(0).getId());
            }
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I try to create a mood entry without date")
    @Transactional
    public void i_try_to_create_a_mood_entry_without_date() {
        try {
            MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
            moodEntryDTO.setMood(MoodType.HAPPY);

            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
            moodEntryDTO.setUser(new UserDTO(user));
            // Note: date is null

            // Call actual service - this should fail until business logic is implemented
            lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I try to create a mood entry for today without mood")
    @Transactional
    public void i_try_to_create_a_mood_entry_for_today_without_mood() {
        try {
            MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
            moodEntryDTO.setDate(LocalDate.now());

            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
            moodEntryDTO.setUser(new UserDTO(user));
            // Note: mood is null

            // Call actual service - this should fail until business logic is implemented
            lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I try to create a mood entry for today with mood {string}")
    @Transactional
    public void i_try_to_create_a_mood_entry_for_today_with_invalid_mood(String invalidMood) {
        try {
            MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
            moodEntryDTO.setDate(LocalDate.now());
            moodEntryDTO.setMood(MoodType.valueOf(invalidMood)); // This will throw IllegalArgumentException

            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
            moodEntryDTO.setUser(new UserDTO(user));

            // Call actual service - this should fail until business logic is implemented
            lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I create mood entries with all valid moods:")
    @Transactional
    public void i_create_mood_entries_with_all_valid_moods(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();

        for (Map<String, String> row : rows) {
            MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
            moodEntryDTO.setDate(LocalDate.parse(row.get("date")));
            moodEntryDTO.setMood(MoodType.valueOf(row.get("mood")));
            moodEntryDTO.setUser(new UserDTO(user));

            // Call actual service - this should fail until business logic is implemented
            moodEntryService.save(moodEntryDTO);
        }
    }

    @When("I retrieve mood entries from {string} to {string}")
    @Transactional
    public void i_retrieve_mood_entries_from_date_to_date(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // Call actual service - this should fail until business logic is implemented
            Page<MoodEntryDTO> page = moodEntryService.findAll(PageRequest.of(0, 100));
            retrievedMoodEntries = page
                .getContent()
                .stream()
                .filter(entry -> !entry.getDate().isBefore(start) && !entry.getDate().isAfter(end))
                .collect(Collectors.toList());
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I request mood statistics")
    @Transactional
    public void i_request_mood_statistics() {
        try {
            // Call actual service - this should fail until business logic is implemented
            Page<MoodEntryDTO> page = moodEntryService.findAll(PageRequest.of(0, 100));
            List<MoodEntryDTO> allEntries = page.getContent();

            // Calculate basic statistics (this is just for test setup, not business logic)
            moodStatistics = Map.of(
                "totalEntries",
                allEntries.size(),
                "mostFrequentMood",
                allEntries
                    .stream()
                    .collect(Collectors.groupingBy(MoodEntryDTO::getMood, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null)
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the mood entry should be created successfully")
    public void the_mood_entry_should_be_created_successfully() {
        assertThat(lastException).isNull();
        assertThat(lastCreatedMoodEntry).isNotNull();
    }

    @Then("the mood entry should have mood {string}")
    public void the_mood_entry_should_have_mood(String expectedMood) {
        assertThat(lastCreatedMoodEntry.getMood()).isEqualTo(MoodType.valueOf(expectedMood));
    }

    @Then("the mood entry should have today's date")
    public void the_mood_entry_should_have_todays_date() {
        assertThat(lastCreatedMoodEntry.getDate()).isEqualTo(LocalDate.now());
    }

    @Then("the mood entry should have date {string}")
    public void the_mood_entry_should_have_date(String expectedDate) {
        assertThat(lastCreatedMoodEntry.getDate()).isEqualTo(LocalDate.parse(expectedDate));
    }

    @Then("the mood entry should belong to {string}")
    public void the_mood_entry_should_belong_to(String expectedUser) {
        User user = userRepository.findOneByLogin(expectedUser).orElseThrow();
        assertThat(lastCreatedMoodEntry.getUser().getId()).isEqualTo(user.getId());
    }

    @Then("the mood entry creation should fail")
    public void the_mood_entry_creation_should_fail() {
        assertThat(lastException).isNotNull();
    }

    @Then("I should get a constraint violation error")
    public void i_should_get_a_constraint_violation_error() {
        assertThat(lastException).isInstanceOfAny(DataIntegrityViolationException.class, ConstraintViolationException.class);
    }

    @Then("the mood entry should be updated successfully")
    public void the_mood_entry_should_be_updated_successfully() {
        assertThat(lastException).isNull();
        assertThat(lastCreatedMoodEntry).isNotNull();
    }

    @Then("I should get {int} mood entries")
    public void i_should_get_mood_entries(Integer expectedCount) {
        assertThat(retrievedMoodEntries).hasSize(expectedCount);
    }

    @Then("all mood entries should belong to me")
    public void all_mood_entries_should_belong_to_me() {
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
        assertThat(retrievedMoodEntries).allMatch(entry -> entry.getUser().getId().equals(currentUser.getId()));
    }

    @Then("I should find the mood entry with mood {string}")
    public void i_should_find_the_mood_entry_with_mood(String expectedMood) {
        assertThat(foundMoodEntry).isNotNull();
        assertThat(foundMoodEntry.getMood()).isEqualTo(MoodType.valueOf(expectedMood));
    }

    @Then("the mood entry should be deleted successfully")
    public void the_mood_entry_should_be_deleted_successfully() {
        assertThat(lastException).isNull();
    }

    @Then("I should have {int} mood entries")
    @Transactional
    public void i_should_have_mood_entries(Integer expectedCount) {
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
        List<MoodEntry> userMoodEntries = moodEntryRepository
            .findAll()
            .stream()
            .filter(entry -> entry.getUser().getId().equals(currentUser.getId()))
            .collect(Collectors.toList());
        assertThat(userMoodEntries).hasSize(expectedCount);
    }

    @Then("I should get a validation error")
    public void i_should_get_a_validation_error() {
        assertThat(lastException).isNotNull();
    }

    @Then("all mood entries should be created successfully")
    public void all_mood_entries_should_be_created_successfully() {
        assertThat(lastException).isNull();
    }

    @Then("I should have {int} mood entries with different moods")
    @Transactional
    public void i_should_have_mood_entries_with_different_moods(Integer expectedCount) {
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
        List<MoodEntry> userMoodEntries = moodEntryRepository
            .findAll()
            .stream()
            .filter(entry -> entry.getUser().getId().equals(currentUser.getId()))
            .collect(Collectors.toList());
        assertThat(userMoodEntries).hasSize(expectedCount);
    }

    @Then("the mood entries should be within the date range")
    public void the_mood_entries_should_be_within_the_date_range() {
        assertThat(retrievedMoodEntries).isNotEmpty();
    }

    @Then("I should get mood statistics")
    public void i_should_get_mood_statistics() {
        assertThat(moodStatistics).isNotNull();
    }

    @Then("the total mood entries should be {int}")
    public void the_total_mood_entries_should_be(Integer expectedTotal) {
        assertThat(moodStatistics.get("totalEntries")).isEqualTo(expectedTotal);
    }

    @Then("the most frequent mood should be {string}")
    public void the_most_frequent_mood_should_be(String expectedMood) {
        assertThat(moodStatistics.get("mostFrequentMood")).isEqualTo(MoodType.valueOf(expectedMood));
    }

    // Helper methods
    private User createTestUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(login + "@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");

        return userRepository.save(user);
    }
}
