package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.MoodEntryAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.MoodEntryService;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.service.mapper.MoodEntryMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link MoodEntryResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MoodEntryResourceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final MoodType DEFAULT_MOOD = MoodType.HAPPY;
    private static final MoodType UPDATED_MOOD = MoodType.SAD;

    private static final String ENTITY_API_URL = "/api/mood-entries";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private MoodEntryRepository moodEntryRepositoryMock;

    @Autowired
    private MoodEntryMapper moodEntryMapper;

    @Mock
    private MoodEntryService moodEntryServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMoodEntryMockMvc;

    private MoodEntry moodEntry;

    private MoodEntry insertedMoodEntry;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MoodEntry createEntity(EntityManager em) {
        MoodEntry moodEntry = new MoodEntry().date(DEFAULT_DATE).mood(DEFAULT_MOOD);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        moodEntry.setUser(user);
        return moodEntry;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MoodEntry createUpdatedEntity(EntityManager em) {
        MoodEntry updatedMoodEntry = new MoodEntry().date(UPDATED_DATE).mood(UPDATED_MOOD);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedMoodEntry.setUser(user);
        return updatedMoodEntry;
    }

    @BeforeEach
    void initTest() {
        moodEntry = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedMoodEntry != null) {
            moodEntryRepository.delete(insertedMoodEntry);
            insertedMoodEntry = null;
        }
    }

    @Test
    @Transactional
    void createMoodEntry() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MoodEntry
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);
        var returnedMoodEntryDTO = om.readValue(
            restMoodEntryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moodEntryDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MoodEntryDTO.class
        );

        // Validate the MoodEntry in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMoodEntry = moodEntryMapper.toEntity(returnedMoodEntryDTO);
        assertMoodEntryUpdatableFieldsEquals(returnedMoodEntry, getPersistedMoodEntry(returnedMoodEntry));

        insertedMoodEntry = returnedMoodEntry;
    }

    @Test
    @Transactional
    void createMoodEntryWithExistingId() throws Exception {
        // Create the MoodEntry with an existing ID
        moodEntry.setId(1L);
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMoodEntryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moodEntryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MoodEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        moodEntry.setDate(null);

        // Create the MoodEntry, which fails.
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        restMoodEntryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moodEntryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMoodIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        moodEntry.setMood(null);

        // Create the MoodEntry, which fails.
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        restMoodEntryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moodEntryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMoodEntries() throws Exception {
        // Initialize the database
        insertedMoodEntry = moodEntryRepository.saveAndFlush(moodEntry);

        // Get all the moodEntryList
        restMoodEntryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(moodEntry.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].mood").value(hasItem(DEFAULT_MOOD.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMoodEntriesWithEagerRelationshipsIsEnabled() throws Exception {
        when(moodEntryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMoodEntryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(moodEntryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMoodEntriesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(moodEntryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMoodEntryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(moodEntryRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getMoodEntry() throws Exception {
        // Initialize the database
        insertedMoodEntry = moodEntryRepository.saveAndFlush(moodEntry);

        // Get the moodEntry
        restMoodEntryMockMvc
            .perform(get(ENTITY_API_URL_ID, moodEntry.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(moodEntry.getId().intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.mood").value(DEFAULT_MOOD.toString()));
    }

    @Test
    @Transactional
    void getNonExistingMoodEntry() throws Exception {
        // Get the moodEntry
        restMoodEntryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMoodEntry() throws Exception {
        // Initialize the database
        insertedMoodEntry = moodEntryRepository.saveAndFlush(moodEntry);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the moodEntry
        MoodEntry updatedMoodEntry = moodEntryRepository.findById(moodEntry.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMoodEntry are not directly saved in db
        em.detach(updatedMoodEntry);
        updatedMoodEntry.date(UPDATED_DATE).mood(UPDATED_MOOD);
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(updatedMoodEntry);

        restMoodEntryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, moodEntryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(moodEntryDTO))
            )
            .andExpect(status().isOk());

        // Validate the MoodEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMoodEntryToMatchAllProperties(updatedMoodEntry);
    }

    @Test
    @Transactional
    void putNonExistingMoodEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moodEntry.setId(longCount.incrementAndGet());

        // Create the MoodEntry
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMoodEntryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, moodEntryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(moodEntryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MoodEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMoodEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moodEntry.setId(longCount.incrementAndGet());

        // Create the MoodEntry
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMoodEntryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(moodEntryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MoodEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMoodEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moodEntry.setId(longCount.incrementAndGet());

        // Create the MoodEntry
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMoodEntryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moodEntryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MoodEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMoodEntryWithPatch() throws Exception {
        // Initialize the database
        insertedMoodEntry = moodEntryRepository.saveAndFlush(moodEntry);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the moodEntry using partial update
        MoodEntry partialUpdatedMoodEntry = new MoodEntry();
        partialUpdatedMoodEntry.setId(moodEntry.getId());

        partialUpdatedMoodEntry.mood(UPDATED_MOOD);

        restMoodEntryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMoodEntry.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMoodEntry))
            )
            .andExpect(status().isOk());

        // Validate the MoodEntry in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMoodEntryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMoodEntry, moodEntry),
            getPersistedMoodEntry(moodEntry)
        );
    }

    @Test
    @Transactional
    void fullUpdateMoodEntryWithPatch() throws Exception {
        // Initialize the database
        insertedMoodEntry = moodEntryRepository.saveAndFlush(moodEntry);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the moodEntry using partial update
        MoodEntry partialUpdatedMoodEntry = new MoodEntry();
        partialUpdatedMoodEntry.setId(moodEntry.getId());

        partialUpdatedMoodEntry.date(UPDATED_DATE).mood(UPDATED_MOOD);

        restMoodEntryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMoodEntry.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMoodEntry))
            )
            .andExpect(status().isOk());

        // Validate the MoodEntry in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMoodEntryUpdatableFieldsEquals(partialUpdatedMoodEntry, getPersistedMoodEntry(partialUpdatedMoodEntry));
    }

    @Test
    @Transactional
    void patchNonExistingMoodEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moodEntry.setId(longCount.incrementAndGet());

        // Create the MoodEntry
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMoodEntryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, moodEntryDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(moodEntryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MoodEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMoodEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moodEntry.setId(longCount.incrementAndGet());

        // Create the MoodEntry
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMoodEntryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(moodEntryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MoodEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMoodEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moodEntry.setId(longCount.incrementAndGet());

        // Create the MoodEntry
        MoodEntryDTO moodEntryDTO = moodEntryMapper.toDto(moodEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMoodEntryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(moodEntryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MoodEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMoodEntry() throws Exception {
        // Initialize the database
        insertedMoodEntry = moodEntryRepository.saveAndFlush(moodEntry);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the moodEntry
        restMoodEntryMockMvc
            .perform(delete(ENTITY_API_URL_ID, moodEntry.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return moodEntryRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected MoodEntry getPersistedMoodEntry(MoodEntry moodEntry) {
        return moodEntryRepository.findById(moodEntry.getId()).orElseThrow();
    }

    protected void assertPersistedMoodEntryToMatchAllProperties(MoodEntry expectedMoodEntry) {
        assertMoodEntryAllPropertiesEquals(expectedMoodEntry, getPersistedMoodEntry(expectedMoodEntry));
    }

    protected void assertPersistedMoodEntryToMatchUpdatableProperties(MoodEntry expectedMoodEntry) {
        assertMoodEntryAllUpdatablePropertiesEquals(expectedMoodEntry, getPersistedMoodEntry(expectedMoodEntry));
    }
}
