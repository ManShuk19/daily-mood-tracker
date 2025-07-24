package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import com.mycompany.myapp.web.rest.TestUtil;
import com.mycompany.myapp.web.rest.UserResourceIT;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link MoodEntryService}.
 */
@IntegrationTest
@Transactional
class MoodEntryServiceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.of(2024, 1, 15);
    private static final LocalDate UPDATED_DATE = LocalDate.of(2024, 1, 16);

    private static final MoodType DEFAULT_MOOD = MoodType.HAPPY;
    private static final MoodType UPDATED_MOOD = MoodType.SAD;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoodEntryService moodEntryService;

    @Autowired
    private EntityManager em;

    private MoodEntry moodEntry;
    private User user;

    public static MoodEntry createEntity(EntityManager em) {
        MoodEntry moodEntry = new MoodEntry().date(DEFAULT_DATE).mood(DEFAULT_MOOD);
        // Add required User entity
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            user = UserResourceIT.createEntity();
            user.setLogin("testuser");
            em.persist(user);
            em.flush();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        moodEntry.setUser(user);
        return moodEntry;
    }

    public static UserDTO createUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        return dto;
    }

    public static MoodEntryDTO createMoodEntryDTO(MoodEntry moodEntry) {
        MoodEntryDTO dto = new MoodEntryDTO();
        dto.setId(moodEntry.getId());
        dto.setDate(moodEntry.getDate());
        dto.setMood(moodEntry.getMood());
        if (moodEntry.getUser() != null) {
            dto.setUser(createUserDTO(moodEntry.getUser()));
        }
        return dto;
    }

    @BeforeEach
    public void initTest() {
        user = UserResourceIT.createEntity();
        user.setLogin("testuser");
        em.persist(user);
        em.flush();

        moodEntry = createEntity(em);
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void createMoodEntry() {
        int databaseSizeBeforeCreate = moodEntryRepository.findAll().size();

        // Create the MoodEntry
        MoodEntryDTO moodEntryDTO = createMoodEntryDTO(moodEntry);
        MoodEntryDTO result = moodEntryService.save(moodEntryDTO);

        // Validate the MoodEntry in the database
        List<MoodEntry> moodEntryList = moodEntryRepository.findAll();
        assertThat(moodEntryList).hasSize(databaseSizeBeforeCreate + 1);

        MoodEntry testMoodEntry = moodEntryList.get(moodEntryList.size() - 1);
        assertThat(testMoodEntry.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testMoodEntry.getMood()).isEqualTo(DEFAULT_MOOD);
        assertThat(testMoodEntry.getUser().getLogin()).isEqualTo("testuser");
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void saveMoodEntryAutomaticallyAssignsCurrentUser() {
        // Create a mood entry DTO without user
        MoodEntryDTO moodEntryDTO = new MoodEntryDTO();
        moodEntryDTO.setDate(DEFAULT_DATE);
        moodEntryDTO.setMood(DEFAULT_MOOD);

        // Save the mood entry
        MoodEntryDTO result = moodEntryService.save(moodEntryDTO);

        // Verify that the current user was automatically assigned
        assertThat(result.getUser().getLogin()).isEqualTo("testuser");
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void getAllMoodEntriesForCurrentUser() {
        // Initialize the database
        moodEntryRepository.saveAndFlush(moodEntry);

        // Create another user and mood entry
        User anotherUser = UserResourceIT.createEntity();
        anotherUser.setLogin("anotheruser");
        em.persist(anotherUser);
        em.flush();

        MoodEntry anotherMoodEntry = new MoodEntry().date(UPDATED_DATE).mood(UPDATED_MOOD).user(anotherUser);
        moodEntryRepository.saveAndFlush(anotherMoodEntry);

        // Get all mood entries for current user
        Pageable pageable = PageRequest.of(0, 20);
        Page<MoodEntryDTO> result = moodEntryService.findAllForCurrentUser(pageable);

        // Verify only current user's entries are returned
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getLogin()).isEqualTo("testuser");
        assertThat(result.getContent().get(0).getMood()).isEqualTo(DEFAULT_MOOD);
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void getAllMoodEntriesForCurrentUserWithoutPagination() {
        // Initialize the database
        moodEntryRepository.saveAndFlush(moodEntry);

        // Create another mood entry for the same user
        MoodEntry anotherMoodEntry = new MoodEntry().date(UPDATED_DATE).mood(UPDATED_MOOD).user(user);
        moodEntryRepository.saveAndFlush(anotherMoodEntry);

        // Get all mood entries for current user
        List<MoodEntryDTO> result = moodEntryService.findAllForCurrentUser();

        // Verify both entries are returned
        assertThat(result).hasSize(2);
        assertThat(result).extracting(MoodEntryDTO::getMood).containsExactlyInAnyOrder(DEFAULT_MOOD, UPDATED_MOOD);
        assertThat(result).extracting(dto -> dto.getUser().getLogin()).allMatch("testuser"::equals);
    }

    @Test
    @Transactional
    void updateMoodEntry() {
        // Initialize the database
        moodEntryRepository.saveAndFlush(moodEntry);

        int databaseSizeBeforeUpdate = moodEntryRepository.findAll().size();

        // Update the moodEntry
        MoodEntry updatedMoodEntry = moodEntryRepository.findById(moodEntry.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMoodEntry are not directly saved in db
        em.detach(updatedMoodEntry);
        updatedMoodEntry.date(UPDATED_DATE).mood(UPDATED_MOOD);
        MoodEntryDTO moodEntryDTO = createMoodEntryDTO(updatedMoodEntry);

        MoodEntryDTO result = moodEntryService.update(moodEntryDTO);

        // Validate the MoodEntry in the database
        List<MoodEntry> moodEntryList = moodEntryRepository.findAll();
        assertThat(moodEntryList).hasSize(databaseSizeBeforeUpdate);
        MoodEntry testMoodEntry = moodEntryList.get(moodEntryList.size() - 1);
        assertThat(testMoodEntry.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testMoodEntry.getMood()).isEqualTo(UPDATED_MOOD);
    }

    @Test
    @Transactional
    void deleteMoodEntry() {
        // Initialize the database
        moodEntryRepository.saveAndFlush(moodEntry);

        int databaseSizeBeforeDelete = moodEntryRepository.findAll().size();

        // Delete the moodEntry
        moodEntryService.delete(moodEntry.getId());

        // Validate the database is empty
        List<MoodEntry> moodEntryList = moodEntryRepository.findAll();
        assertThat(moodEntryList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
