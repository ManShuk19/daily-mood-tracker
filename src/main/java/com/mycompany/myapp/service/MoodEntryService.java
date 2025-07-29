package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.service.dto.MoodStatisticsDTO;
import com.mycompany.myapp.service.dto.MoodTrendDTO;
import com.mycompany.myapp.service.mapper.MoodEntryMapper;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.MoodEntry}.
 */
@Service
@Transactional
public class MoodEntryService {

    private static final Logger LOG = LoggerFactory.getLogger(MoodEntryService.class);

    private final MoodEntryRepository moodEntryRepository;

    private final MoodEntryMapper moodEntryMapper;

    private final UserRepository userRepository;

    public MoodEntryService(MoodEntryRepository moodEntryRepository, MoodEntryMapper moodEntryMapper, UserRepository userRepository) {
        this.moodEntryRepository = moodEntryRepository;
        this.moodEntryMapper = moodEntryMapper;
        this.userRepository = userRepository;
    }

    /**
     * Save a moodEntry.
     *
     * @param moodEntryDTO the entity to save.
     * @return the persisted entity.
     */
    public MoodEntryDTO save(MoodEntryDTO moodEntryDTO) {
        LOG.debug("Request to save MoodEntry : {}", moodEntryDTO);
        MoodEntry moodEntry = moodEntryMapper.toEntity(moodEntryDTO);

        // Automatically assign current user if not already set
        if (moodEntry.getUser() == null) {
            SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin).ifPresent(moodEntry::setUser);
        }

        moodEntry = moodEntryRepository.save(moodEntry);
        return moodEntryMapper.toDto(moodEntry);
    }

    /**
     * Update a moodEntry.
     *
     * @param moodEntryDTO the entity to save.
     * @return the persisted entity.
     */
    public MoodEntryDTO update(MoodEntryDTO moodEntryDTO) {
        LOG.debug("Request to update MoodEntry : {}", moodEntryDTO);
        MoodEntry moodEntry = moodEntryMapper.toEntity(moodEntryDTO);
        moodEntry = moodEntryRepository.save(moodEntry);
        return moodEntryMapper.toDto(moodEntry);
    }

    /**
     * Partially update a moodEntry.
     *
     * @param moodEntryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MoodEntryDTO> partialUpdate(MoodEntryDTO moodEntryDTO) {
        LOG.debug("Request to partially update MoodEntry : {}", moodEntryDTO);

        return moodEntryRepository
            .findById(moodEntryDTO.getId())
            .map(existingMoodEntry -> {
                moodEntryMapper.partialUpdate(existingMoodEntry, moodEntryDTO);

                return existingMoodEntry;
            })
            .map(moodEntryRepository::save)
            .map(moodEntryMapper::toDto);
    }

    /**
     * Get all the moodEntries.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MoodEntryDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all MoodEntries");
        return moodEntryRepository.findAll(pageable).map(moodEntryMapper::toDto);
    }

    /**
     * Get all the moodEntries with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<MoodEntryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return moodEntryRepository.findAllWithEagerRelationships(pageable).map(moodEntryMapper::toDto);
    }

    /**
     * Get one moodEntry by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MoodEntryDTO> findOne(Long id) {
        LOG.debug("Request to get MoodEntry : {}", id);
        return moodEntryRepository.findOneWithEagerRelationships(id).map(moodEntryMapper::toDto);
    }

    /**
     * Delete the moodEntry by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete MoodEntry : {}", id);
        moodEntryRepository.deleteById(id);
    }

    /**
     * Get all mood entries for the current user.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MoodEntryDTO> findAllForCurrentUser(Pageable pageable) {
        LOG.debug("Request to get all MoodEntries for current user");
        return moodEntryRepository.findByUserIsCurrentUserOrderByDateDesc(pageable).map(moodEntryMapper::toDto);
    }

    /**
     * Get mood entry for current user by date.
     *
     * @param date the date to search for.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MoodEntryDTO> findByCurrentUserAndDate(LocalDate date) {
        LOG.debug("Request to get MoodEntry for current user on date : {}", date);
        return moodEntryRepository.findByUserIsCurrentUserAndDate(date).map(moodEntryMapper::toDto);
    }

    /**
     * Get mood entries for current user within date range.
     *
     * @param startDate the start date.
     * @param endDate the end date.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<MoodEntryDTO> findByCurrentUserAndDateBetween(LocalDate startDate, LocalDate endDate) {
        LOG.debug("Request to get MoodEntries for current user between {} and {}", startDate, endDate);
        return moodEntryRepository
            .findByUserIsCurrentUserAndDateBetween(startDate, endDate)
            .stream()
            .map(moodEntryMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get mood statistics for current user.
     *
     * @param startDate the start date for statistics.
     * @param endDate the end date for statistics.
     * @return the mood statistics.
     */
    @Transactional(readOnly = true)
    public MoodStatisticsDTO getMoodStatisticsForCurrentUser(LocalDate startDate, LocalDate endDate) {
        LOG.debug("Request to get mood statistics for current user between {} and {}", startDate, endDate);

        List<MoodEntry> entries = moodEntryRepository.findByUserIsCurrentUserAndDateBetween(startDate, endDate);

        MoodStatisticsDTO statistics = new MoodStatisticsDTO();
        statistics.setStartDate(startDate);
        statistics.setEndDate(endDate);
        statistics.setTotalEntries((long) entries.size());

        if (entries.isEmpty()) {
            statistics.setMoodDistribution(new HashMap<>());
            statistics.setTrends(new ArrayList<>());
            statistics.setAverageMoodScore(0.0);
            statistics.setTrackingCompletionRate(0.0);
            return statistics;
        }

        // Calculate mood distribution
        Map<MoodType, Long> moodDistribution = entries.stream().collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.counting()));
        statistics.setMoodDistribution(moodDistribution);

        // Find most frequent mood
        Map.Entry<MoodType, Long> mostFrequent = moodDistribution.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);

        if (mostFrequent != null) {
            statistics.setMostFrequentMood(mostFrequent.getKey());
            statistics.setMostFrequentMoodCount(mostFrequent.getValue());
        }

        // Calculate average mood score
        double averageScore = entries.stream().mapToInt(entry -> getMoodScore(entry.getMood())).average().orElse(0.0);
        statistics.setAverageMoodScore(averageScore);

        // Calculate trends
        List<MoodTrendDTO> trends = entries
            .stream()
            .sorted(Comparator.comparing(MoodEntry::getDate))
            .map(entry -> new MoodTrendDTO(entry.getDate(), entry.getMood()))
            .collect(Collectors.toList());
        statistics.setTrends(trends);

        // Calculate streaks
        calculateStreaks(entries, statistics);

        // Calculate tracking completion rate
        long daysInRange = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double completionRate = ((double) entries.size() / daysInRange) * 100;
        statistics.setTrackingCompletionRate(completionRate);

        return statistics;
    }

    /**
     * Get mood statistics for current user for the current month.
     *
     * @return the mood statistics.
     */
    @Transactional(readOnly = true)
    public MoodStatisticsDTO getMoodStatisticsForCurrentMonth() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        return getMoodStatisticsForCurrentUser(startOfMonth, endOfMonth);
    }

    /**
     * Get mood statistics for current user for the last 7 days.
     *
     * @return the mood statistics.
     */
    @Transactional(readOnly = true)
    public MoodStatisticsDTO getMoodStatisticsForLastWeek() {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(6);
        return getMoodStatisticsForCurrentUser(startOfWeek, now);
    }

    private int getMoodScore(MoodType mood) {
        if (mood == null) {
            return 0;
        }
        switch (mood) {
            case HAPPY:
                return 5;
            case NEUTRAL:
                return 3;
            case ANXIOUS:
                return 2;
            case SAD:
                return 1;
            case ANGRY:
                return 0;
            default:
                return 0;
        }
    }

    private void calculateStreaks(List<MoodEntry> entries, MoodStatisticsDTO statistics) {
        if (entries.isEmpty()) {
            return;
        }

        // Sort entries by date
        List<MoodEntry> sortedEntries = entries.stream().sorted(Comparator.comparing(MoodEntry::getDate)).collect(Collectors.toList());

        // Calculate current streak
        MoodEntry latestEntry = sortedEntries.get(sortedEntries.size() - 1);
        LocalDate currentDate = LocalDate.now();
        LocalDate latestEntryDate = latestEntry.getDate();

        if (latestEntryDate.equals(currentDate) || latestEntryDate.equals(currentDate.minusDays(1))) {
            // Calculate current streak
            long currentStreak = 1;
            MoodType currentMood = latestEntry.getMood();

            for (int i = sortedEntries.size() - 2; i >= 0; i--) {
                MoodEntry entry = sortedEntries.get(i);
                if (entry.getMood() == currentMood && entry.getDate().equals(latestEntryDate.minusDays(currentStreak))) {
                    currentStreak++;
                } else {
                    break;
                }
            }

            statistics.setCurrentStreak(currentStreak);
            statistics.setCurrentStreakMood(currentMood);
        }

        // Calculate longest streak
        long longestStreak = 1;
        MoodType longestStreakMood = sortedEntries.get(0).getMood();
        long tempStreak = 1;
        MoodType tempMood = sortedEntries.get(0).getMood();

        for (int i = 1; i < sortedEntries.size(); i++) {
            MoodEntry entry = sortedEntries.get(i);
            if (entry.getMood() == tempMood) {
                tempStreak++;
            } else {
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak;
                    longestStreakMood = tempMood;
                }
                tempStreak = 1;
                tempMood = entry.getMood();
            }
        }

        // Check if the last streak is the longest
        if (tempStreak > longestStreak) {
            longestStreak = tempStreak;
            longestStreakMood = tempMood;
        }

        statistics.setLongestStreak(longestStreak);
        statistics.setLongestStreakMood(longestStreakMood);
    }
}
