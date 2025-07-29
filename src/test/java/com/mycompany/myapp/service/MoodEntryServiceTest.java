package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.service.dto.MoodStatisticsDTO;
import com.mycompany.myapp.service.mapper.MoodEntryMapper;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Integration tests for {@link MoodEntryService}.
 */
@ExtendWith(MockitoExtension.class)
class MoodEntryServiceTest {

    @Mock
    private MoodEntryRepository moodEntryRepository;

    @Mock
    private MoodEntryMapper moodEntryMapper;

    @InjectMocks
    private MoodEntryService moodEntryService;

    private User testUser;
    private MoodEntry testMoodEntry;
    private MoodEntryDTO testMoodEntryDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");

        testMoodEntry = new MoodEntry();
        testMoodEntry.setId(1L);
        testMoodEntry.setDate(LocalDate.now());
        testMoodEntry.setMood(MoodType.HAPPY);
        testMoodEntry.setUser(testUser);

        testMoodEntryDTO = new MoodEntryDTO();
        testMoodEntryDTO.setId(1L);
        testMoodEntryDTO.setDate(LocalDate.now());
        testMoodEntryDTO.setMood(MoodType.HAPPY);
    }

    @Test
    void shouldSaveMoodEntry() {
        // given
        when(moodEntryMapper.toEntity(any(MoodEntryDTO.class))).thenReturn(testMoodEntry);
        when(moodEntryRepository.save(any(MoodEntry.class))).thenReturn(testMoodEntry);
        when(moodEntryMapper.toDto(any(MoodEntry.class))).thenReturn(testMoodEntryDTO);

        // when
        MoodEntryDTO result = moodEntryService.save(testMoodEntryDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMood()).isEqualTo(MoodType.HAPPY);
        verify(moodEntryRepository).save(any(MoodEntry.class));
    }

    @Test
    void shouldUpdateMoodEntry() {
        // given
        when(moodEntryMapper.toEntity(any(MoodEntryDTO.class))).thenReturn(testMoodEntry);
        when(moodEntryRepository.save(any(MoodEntry.class))).thenReturn(testMoodEntry);
        when(moodEntryMapper.toDto(any(MoodEntry.class))).thenReturn(testMoodEntryDTO);

        // when
        MoodEntryDTO result = moodEntryService.update(testMoodEntryDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(moodEntryRepository).save(any(MoodEntry.class));
    }

    @Test
    void shouldFindAllMoodEntries() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MoodEntry> moodEntryPage = new PageImpl<>(Arrays.asList(testMoodEntry));
        when(moodEntryRepository.findAll(pageable)).thenReturn(moodEntryPage);
        when(moodEntryMapper.toDto(any(MoodEntry.class))).thenReturn(testMoodEntryDTO);

        // when
        Page<MoodEntryDTO> result = moodEntryService.findAll(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void shouldFindOneMoodEntry() {
        // given
        when(moodEntryRepository.findOneWithEagerRelationships(1L)).thenReturn(Optional.of(testMoodEntry));
        when(moodEntryMapper.toDto(any(MoodEntry.class))).thenReturn(testMoodEntryDTO);

        // when
        Optional<MoodEntryDTO> result = moodEntryService.findOne(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(1L);
    }

    @Test
    void shouldDeleteMoodEntry() {
        // given
        doNothing().when(moodEntryRepository).deleteById(1L);

        // when
        moodEntryService.delete(1L);

        // then
        verify(moodEntryRepository).deleteById(1L);
    }

    @Test
    void shouldFindAllForCurrentUser() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MoodEntry> moodEntryPage = new PageImpl<>(Arrays.asList(testMoodEntry));
        when(moodEntryRepository.findByUserIsCurrentUserOrderByDateDesc(pageable)).thenReturn(moodEntryPage);
        when(moodEntryMapper.toDto(any(MoodEntry.class))).thenReturn(testMoodEntryDTO);

        // when
        Page<MoodEntryDTO> result = moodEntryService.findAllForCurrentUser(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(moodEntryRepository).findByUserIsCurrentUserOrderByDateDesc(pageable);
    }

    @Test
    void shouldFindByCurrentUserAndDate() {
        // given
        LocalDate testDate = LocalDate.now();
        when(moodEntryRepository.findByUserIsCurrentUserAndDate(testDate)).thenReturn(Optional.of(testMoodEntry));
        when(moodEntryMapper.toDto(any(MoodEntry.class))).thenReturn(testMoodEntryDTO);

        // when
        Optional<MoodEntryDTO> result = moodEntryService.findByCurrentUserAndDate(testDate);

        // then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getDate()).isEqualTo(testDate);
        verify(moodEntryRepository).findByUserIsCurrentUserAndDate(testDate);
    }

    @Test
    void shouldFindByCurrentUserAndDateBetween() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<MoodEntry> moodEntries = Arrays.asList(testMoodEntry);
        when(moodEntryRepository.findByUserIsCurrentUserAndDateBetween(startDate, endDate)).thenReturn(moodEntries);
        when(moodEntryMapper.toDto(any(MoodEntry.class))).thenReturn(testMoodEntryDTO);

        // when
        List<MoodEntryDTO> result = moodEntryService.findByCurrentUserAndDateBetween(startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(moodEntryRepository).findByUserIsCurrentUserAndDateBetween(startDate, endDate);
    }

    @Test
    void shouldGetMoodStatisticsForCurrentUser() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        MoodEntry happyEntry = new MoodEntry();
        happyEntry.setMood(MoodType.HAPPY);
        happyEntry.setDate(LocalDate.now().minusDays(1));
        happyEntry.setUser(testUser);

        MoodEntry sadEntry = new MoodEntry();
        sadEntry.setMood(MoodType.SAD);
        sadEntry.setDate(LocalDate.now());
        sadEntry.setUser(testUser);

        List<MoodEntry> moodEntries = Arrays.asList(happyEntry, sadEntry);
        when(moodEntryRepository.findByUserIsCurrentUserAndDateBetween(startDate, endDate)).thenReturn(moodEntries);

        // when
        MoodStatisticsDTO result = moodEntryService.getMoodStatisticsForCurrentUser(startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalEntries()).isEqualTo(2L);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getMoodDistribution()).containsKeys(MoodType.HAPPY, MoodType.SAD);
        assertThat(result.getMoodDistribution().get(MoodType.HAPPY)).isEqualTo(1L);
        assertThat(result.getMoodDistribution().get(MoodType.SAD)).isEqualTo(1L);
        assertThat(result.getAverageMoodScore()).isEqualTo(3.0); // (5 + 1) / 2
    }

    @Test
    void shouldGetMoodStatisticsForCurrentMonth() {
        // given
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        when(moodEntryRepository.findByUserIsCurrentUserAndDateBetween(startOfMonth, endOfMonth)).thenReturn(Arrays.asList(testMoodEntry));

        // when
        MoodStatisticsDTO result = moodEntryService.getMoodStatisticsForCurrentMonth();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(startOfMonth);
        assertThat(result.getEndDate()).isEqualTo(endOfMonth);
        verify(moodEntryRepository).findByUserIsCurrentUserAndDateBetween(startOfMonth, endOfMonth);
    }

    @Test
    void shouldGetMoodStatisticsForLastWeek() {
        // given
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(6);

        when(moodEntryRepository.findByUserIsCurrentUserAndDateBetween(startOfWeek, now)).thenReturn(Arrays.asList(testMoodEntry));

        // when
        MoodStatisticsDTO result = moodEntryService.getMoodStatisticsForLastWeek();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(startOfWeek);
        assertThat(result.getEndDate()).isEqualTo(now);
        verify(moodEntryRepository).findByUserIsCurrentUserAndDateBetween(startOfWeek, now);
    }

    @Test
    void shouldCalculateStreaksCorrectly() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        MoodEntry entry1 = new MoodEntry();
        entry1.setMood(MoodType.HAPPY);
        entry1.setDate(twoDaysAgo);
        entry1.setUser(testUser);

        MoodEntry entry2 = new MoodEntry();
        entry2.setMood(MoodType.HAPPY);
        entry2.setDate(yesterday);
        entry2.setUser(testUser);

        MoodEntry entry3 = new MoodEntry();
        entry3.setMood(MoodType.HAPPY);
        entry3.setDate(today);
        entry3.setUser(testUser);

        List<MoodEntry> moodEntries = Arrays.asList(entry1, entry2, entry3);
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today;

        when(moodEntryRepository.findByUserIsCurrentUserAndDateBetween(startDate, endDate)).thenReturn(moodEntries);

        // when
        MoodStatisticsDTO result = moodEntryService.getMoodStatisticsForCurrentUser(startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentStreak()).isEqualTo(3L);
        assertThat(result.getCurrentStreakMood()).isEqualTo(MoodType.HAPPY);
        assertThat(result.getLongestStreak()).isEqualTo(3L);
        assertThat(result.getLongestStreakMood()).isEqualTo(MoodType.HAPPY);
    }

    @Test
    void shouldHandleEmptyMoodEntries() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(moodEntryRepository.findByUserIsCurrentUserAndDateBetween(startDate, endDate)).thenReturn(Arrays.asList());

        // when
        MoodStatisticsDTO result = moodEntryService.getMoodStatisticsForCurrentUser(startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalEntries()).isEqualTo(0L);
        assertThat(result.getMoodDistribution()).isEmpty();
        assertThat(result.getAverageMoodScore()).isEqualTo(0.0);
        assertThat(result.getTrends()).isEmpty();
    }
}
