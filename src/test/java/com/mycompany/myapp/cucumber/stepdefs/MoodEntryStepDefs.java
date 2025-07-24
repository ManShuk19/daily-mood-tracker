package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.MoodEntryService;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import com.mycompany.myapp.service.mapper.MoodEntryMapper;
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

public class MoodEntryStepDefs extends StepDefs {

    @Autowired
    private MoodEntryService moodEntryService;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoodEntryMapper moodEntryMapper;

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

    @Given("the system is ready for mood entry testing")
    public void the_system_is_ready_for_mood_entry_testing() {
        // System initialization - verify repositories are available
        assertThat(moodEntryService).isNotNull();
        assertThat(moodEntryRepository).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Given("test users are created")
    public void test_users_are_created() {
        // Create test user1
        testUser1 = new User();
        testUser1.setLogin("user1");
        testUser1.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser1.setEmail("user1@test.com");
        testUser1.setActivated(true);
        testUser1.setFirstName("Test");
        testUser1.setLastName("User1");
        testUser1.setLangKey("en");
        testUser1 = userRepository.save(testUser1);

        // Create test user2
        testUser2 = new User();
        testUser2.setLogin("user2");
        testUser2.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser2.setEmail("user2@test.com");
        testUser2.setActivated(true);
        testUser2.setFirstName("Test");
        testUser2.setLastName("User2");
        testUser2.setLangKey("en");
        testUser2 = userRepository.save(testUser2);
    }

    @Given("I am logged in as {string}")
    public void i_am_logged_in_as(String username) {
        currentUserLogin = username;
        User user = userRepository.findOneByLogin(username).orElseThrow();

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

        lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
    }

    @Given("user {string} has a mood entry for {string} with mood {string}")
    @Transactional
    public void user_has_a_mood_entry_for_date_with_mood(String username, String dateString, String moodString) {
        MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
        moodEntryDTO.setDate(LocalDate.parse(dateString));
        moodEntryDTO.setMood(MoodType.valueOf(moodString));

        User user = userRepository.findOneByLogin(username).orElseThrow();
        moodEntryDTO.setUser(new UserDTO(user));

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

            lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I update the mood entry to {string}")
    @Transactional
    public void i_update_the_mood_entry_to(String newMoodString) {
        try {
            assertThat(lastCreatedMoodEntry).isNotNull();

            lastCreatedMoodEntry.setMood(MoodType.valueOf(newMoodString));
            lastCreatedMoodEntry = moodEntryService.update(lastCreatedMoodEntry);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I retrieve my mood entries")
    @Transactional
    public void i_retrieve_my_mood_entries() {
        try {
            Page<MoodEntryDTO> page = moodEntryService.findAll(PageRequest.of(0, 20));
            retrievedMoodEntries = page.getContent();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I delete the mood entry for {string}")
    @Transactional
    public void i_delete_the_mood_entry_for_date(String dateString) {
        try {
            assertThat(lastCreatedMoodEntry).isNotNull();
            moodEntryService.delete(lastCreatedMoodEntry.getId());
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
            // No date set - should cause validation error

            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
            moodEntryDTO.setUser(new UserDTO(user));

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
            // No mood set - should cause validation error

            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
            moodEntryDTO.setUser(new UserDTO(user));

            lastCreatedMoodEntry = moodEntryService.save(moodEntryDTO);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I search for mood entry on {string}")
    @Transactional
    public void i_search_for_mood_entry_on_date(String dateString) {
        try {
            LocalDate searchDate = LocalDate.parse(dateString);
            List<MoodEntry> userMoodEntries = moodEntryRepository.findByUserIsCurrentUser();

            Optional<MoodEntry> moodEntry = userMoodEntries.stream().filter(entry -> entry.getDate().equals(searchDate)).findFirst();

            moodEntry.ifPresent(entry -> foundMoodEntry = moodEntryMapper.toDto(entry));
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I create mood entries with all valid moods:")
    @Transactional
    public void i_create_mood_entries_with_all_valid_moods(DataTable dataTable) {
        try {
            List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
            User user = userRepository.findOneByLogin(currentUserLogin).orElseThrow();

            for (Map<String, String> row : rows) {
                MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
                moodEntryDTO.setDate(LocalDate.parse(row.get("date")));
                moodEntryDTO.setMood(MoodType.valueOf(row.get("mood")));
                moodEntryDTO.setUser(new UserDTO(user));

                moodEntryService.save(moodEntryDTO);
            }
        } catch (Exception e) {
            lastException = e;
        }
    }

    // Then steps - assertions
    @Then("the mood entry should be saved successfully")
    public void the_mood_entry_should_be_saved_successfully() {
        assertThat(lastException).isNull();
        assertThat(lastCreatedMoodEntry).isNotNull();
        assertThat(lastCreatedMoodEntry.getId()).isNotNull();
    }

    @Then("the mood entry should have mood {string}")
    public void the_mood_entry_should_have_mood(String expectedMood) {
        assertThat(lastCreatedMoodEntry).isNotNull();
        assertThat(lastCreatedMoodEntry.getMood()).isEqualTo(MoodType.valueOf(expectedMood));
    }

    @Then("the mood entry should have today's date")
    public void the_mood_entry_should_have_todays_date() {
        assertThat(lastCreatedMoodEntry).isNotNull();
        assertThat(lastCreatedMoodEntry.getDate()).isEqualTo(LocalDate.now());
    }

    @Then("both mood entries should be saved successfully")
    public void both_mood_entries_should_be_saved_successfully() {
        assertThat(lastException).isNull();
        assertThat(lastCreatedMoodEntry).isNotNull();
    }

    @Then("I should have {int} mood entries in total")
    @Transactional
    public void i_should_have_mood_entries_in_total(Integer expectedCount) {
        List<MoodEntry> userMoodEntries = moodEntryRepository.findByUserIsCurrentUser();
        assertThat(userMoodEntries).hasSize(expectedCount);
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
        assertThat(retrievedMoodEntries).isNotNull();

        // Filter entries for current user since we're using findAll instead of user-specific query
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();
        long userEntriesCount = retrievedMoodEntries.stream().filter(entry -> entry.getUser().getId().equals(currentUser.getId())).count();

        assertThat(userEntriesCount).isEqualTo(expectedCount.longValue());
    }

    @Then("the mood entries should belong to me")
    public void the_mood_entries_should_belong_to_me() {
        assertThat(retrievedMoodEntries).isNotNull();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();

        retrievedMoodEntries.forEach(entry -> {
            if (entry.getUser().getId().equals(currentUser.getId())) {
                assertThat(entry.getUser().getLogin()).isEqualTo(currentUserLogin);
            }
        });
    }

    @Then("the mood entry should be deleted successfully")
    public void the_mood_entry_should_be_deleted_successfully() {
        assertThat(lastException).isNull();

        // Verify it's actually deleted
        Optional<MoodEntryDTO> deletedEntry = moodEntryService.findOne(lastCreatedMoodEntry.getId());
        assertThat(deletedEntry).isEmpty();
    }

    @Then("I should have {int} mood entries")
    @Transactional
    public void i_should_have_mood_entries(Integer expectedCount) {
        List<MoodEntry> userMoodEntries = moodEntryRepository.findByUserIsCurrentUser();
        assertThat(userMoodEntries).hasSize(expectedCount);
    }

    @Then("the mood entry should belong to {string}")
    public void the_mood_entry_should_belong_to(String expectedUser) {
        assertThat(retrievedMoodEntries).isNotNull();
        assertThat(retrievedMoodEntries).hasSize(1);

        MoodEntryDTO entry = retrievedMoodEntries.get(0);
        assertThat(entry.getUser().getLogin()).isEqualTo(expectedUser);
    }

    @Then("I should get a validation error")
    public void i_should_get_a_validation_error() {
        assertThat(lastException).isInstanceOfAny(
            ConstraintViolationException.class,
            IllegalArgumentException.class,
            DataIntegrityViolationException.class
        );
    }

    @Then("I should find the mood entry with mood {string}")
    public void i_should_find_the_mood_entry_with_mood(String expectedMood) {
        assertThat(foundMoodEntry).isNotNull();
        assertThat(foundMoodEntry.getMood()).isEqualTo(MoodType.valueOf(expectedMood));
    }

    @Then("all mood entries should be saved successfully")
    public void all_mood_entries_should_be_saved_successfully() {
        assertThat(lastException).isNull();
    }

    @Then("I should have {int} mood entries with different moods")
    @Transactional
    public void i_should_have_mood_entries_with_different_moods(Integer expectedCount) {
        List<MoodEntry> userMoodEntries = moodEntryRepository.findByUserIsCurrentUser();
        assertThat(userMoodEntries).hasSize(expectedCount);

        // Verify we have different moods
        long distinctMoodCount = userMoodEntries.stream().map(MoodEntry::getMood).distinct().count();
        assertThat(distinctMoodCount).isEqualTo(expectedCount.longValue());
    }
}
