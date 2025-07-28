package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.enumeration.MoodType;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.service.dto.MoodStatisticsDTO;
import com.mycompany.myapp.service.dto.MoodStreakDTO;
import com.mycompany.myapp.service.dto.MoodTrendDTO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing mood analytics.
 */
@Service
@Transactional
public class MoodAnalyticsService {

    private static final Logger LOG = LoggerFactory.getLogger(MoodAnalyticsService.class);

    private final MoodEntryRepository moodEntryRepository;

    public MoodAnalyticsService(MoodEntryRepository moodEntryRepository) {
        this.moodEntryRepository = moodEntryRepository;
    }

    /**
     * Get mood statistics for a specific month.
     *
     * @param month the month in format "YYYY-MM"
     * @return the mood statistics
     */
    @Transactional(readOnly = true)
    public MoodStatisticsDTO getMoodStatisticsForMonth(String month) {
        LOG.debug("Request to get mood statistics for month : {}", month);

        // Parse the month string to get start and end dates
        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // Get current user's mood entries for the month
        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MoodEntry> moodEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .filter(entry -> !entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(endDate))
            .collect(Collectors.toList());

        // Calculate statistics
        MoodStatisticsDTO statistics = new MoodStatisticsDTO();
        statistics.setTotalEntries(moodEntries.size());
        statistics.setHappyCount((int) moodEntries.stream().filter(e -> e.getMood() == MoodType.HAPPY).count());
        statistics.setSadCount((int) moodEntries.stream().filter(e -> e.getMood() == MoodType.SAD).count());
        statistics.setAngryCount((int) moodEntries.stream().filter(e -> e.getMood() == MoodType.ANGRY).count());
        statistics.setNeutralCount((int) moodEntries.stream().filter(e -> e.getMood() == MoodType.NEUTRAL).count());
        statistics.setAnxiousCount((int) moodEntries.stream().filter(e -> e.getMood() == MoodType.ANXIOUS).count());
        statistics.setPeriod(month);

        // Find most common mood
        Map<MoodType, Long> moodCounts = moodEntries.stream().collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.counting()));

        Optional<Map.Entry<MoodType, Long>> mostCommon = moodCounts.entrySet().stream().max(Map.Entry.comparingByValue());

        mostCommon.ifPresent(entry -> statistics.setMostCommonMood(entry.getKey()));

        return statistics;
    }

    /**
     * Get mood trend for the last N days.
     *
     * @param days the number of days to look back
     * @return the mood trend
     */
    @Transactional(readOnly = true)
    public MoodTrendDTO getMoodTrendForLastDays(int days) {
        LOG.debug("Request to get mood trend for last {} days", days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Get current user's mood entries for the period
        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MoodEntry> moodEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .filter(entry -> !entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(endDate))
            .sorted(Comparator.comparing(MoodEntry::getDate))
            .collect(Collectors.toList());

        // Create data points
        List<MoodTrendDTO.MoodTrendDataPoint> dataPoints = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            MoodTrendDTO.MoodTrendDataPoint dataPoint = new MoodTrendDTO.MoodTrendDataPoint();
            dataPoint.setDate(date);
            dataPoint.setDayNumber(i + 1);

            // Find mood for this date
            Optional<MoodEntry> entryForDate = moodEntries.stream().filter(entry -> entry.getDate().equals(date)).findFirst();

            entryForDate.ifPresent(entry -> dataPoint.setMood(entry.getMood()));
            dataPoints.add(dataPoint);
        }

        MoodTrendDTO trend = new MoodTrendDTO();
        trend.setDataPoints(dataPoints);
        trend.setTotalDays(days);
        trend.setPeriod("last_" + days + "_days");

        return trend;
    }

    /**
     * Get mood streak information.
     *
     * @return the mood streak information
     */
    @Transactional(readOnly = true)
    public MoodStreakDTO getMoodStreakInformation() {
        LOG.debug("Request to get mood streak information");

        // Get current user's mood entries
        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MoodEntry> moodEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .sorted(Comparator.comparing(MoodEntry::getDate).reversed())
            .collect(Collectors.toList());

        MoodStreakDTO streakInfo = new MoodStreakDTO();

        // Calculate current happy streak
        int currentHappyStreak = 0;
        for (MoodEntry entry : moodEntries) {
            if (entry.getMood() == MoodType.HAPPY) {
                currentHappyStreak++;
            } else {
                break;
            }
        }
        streakInfo.setCurrentHappyStreak(currentHappyStreak);

        // Calculate longest happy streak
        int longestHappyStreak = calculateLongestStreak(moodEntries, MoodType.HAPPY);
        streakInfo.setLongestHappyStreak(longestHappyStreak);

        // Calculate current sad streak
        int currentSadStreak = 0;
        for (MoodEntry entry : moodEntries) {
            if (entry.getMood() == MoodType.SAD) {
                currentSadStreak++;
            } else {
                break;
            }
        }
        streakInfo.setCurrentSadStreak(currentSadStreak);

        // Calculate longest sad streak
        int longestSadStreak = calculateLongestStreak(moodEntries, MoodType.SAD);
        streakInfo.setLongestSadStreak(longestSadStreak);

        // Calculate current anxious streak
        int currentAnxiousStreak = 0;
        for (MoodEntry entry : moodEntries) {
            if (entry.getMood() == MoodType.ANXIOUS) {
                currentAnxiousStreak++;
            } else {
                break;
            }
        }
        streakInfo.setCurrentAnxiousStreak(currentAnxiousStreak);

        // Calculate longest anxious streak
        int longestAnxiousStreak = calculateLongestStreak(moodEntries, MoodType.ANXIOUS);
        streakInfo.setLongestAnxiousStreak(longestAnxiousStreak);

        return streakInfo;
    }

    /**
     * Export mood data for a specific month.
     *
     * @param month the month in format "YYYY-MM"
     * @return CSV formatted mood data
     */
    @Transactional(readOnly = true)
    public String exportMoodDataForMonth(String month) {
        LOG.debug("Request to export mood data for month : {}", month);

        // Parse the month string to get start and end dates
        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // Get current user's mood entries for the month
        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MoodEntry> moodEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .filter(entry -> !entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(endDate))
            .sorted(Comparator.comparing(MoodEntry::getDate))
            .collect(Collectors.toList());

        // Generate CSV
        StringBuilder csv = new StringBuilder();
        csv.append("date,mood\n");

        for (MoodEntry entry : moodEntries) {
            csv.append(entry.getDate()).append(",").append(entry.getMood()).append("\n");
        }

        return csv.toString();
    }

    /**
     * Compare mood patterns between two periods.
     *
     * @param period1 the first period
     * @param period2 the second period
     * @return comparison data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> compareMoodPatterns(String period1, String period2) {
        LOG.debug("Request to compare mood patterns between {} and {}", period1, period2);

        MoodStatisticsDTO stats1 = getMoodStatisticsForMonth(period1);
        MoodStatisticsDTO stats2 = getMoodStatisticsForMonth(period2);

        Map<String, Object> comparison = new HashMap<>();

        // Calculate percentage changes
        Map<String, Object> percentageChanges = new HashMap<>();
        if (stats1.getTotalEntries() > 0) {
            double happyChange = ((double) (stats2.getHappyCount() - stats1.getHappyCount()) / stats1.getTotalEntries()) * 100;
            double sadChange = ((double) (stats2.getSadCount() - stats1.getSadCount()) / stats1.getTotalEntries()) * 100;
            double anxiousChange = ((double) (stats2.getAnxiousCount() - stats1.getAnxiousCount()) / stats1.getTotalEntries()) * 100;

            percentageChanges.put("happy_change", Math.round(happyChange * 100.0) / 100.0);
            percentageChanges.put("sad_change", Math.round(sadChange * 100.0) / 100.0);
            percentageChanges.put("anxious_change", Math.round(anxiousChange * 100.0) / 100.0);
        }

        comparison.put("percentage_changes", percentageChanges);
        comparison.put("period1_stats", stats1);
        comparison.put("period2_stats", stats2);

        // Determine if there's improvement
        boolean improvement = stats2.getHappyCount() > stats1.getHappyCount() && stats2.getSadCount() < stats1.getSadCount();
        comparison.put("improvement", improvement);

        return comparison;
    }

    /**
     * Get mood insights and recommendations.
     *
     * @return insights and recommendations
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMoodInsights() {
        LOG.debug("Request to get mood insights");

        Map<String, Object> insights = new HashMap<>();

        // Get mood statistics for the last 30 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        String currentUserLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MoodEntry> moodEntries = moodEntryRepository
            .findByUserIsCurrentUser()
            .stream()
            .filter(entry -> !entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(endDate))
            .collect(Collectors.toList());

        // Analyze patterns
        List<String> insightsList = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Check for anxious patterns on specific days
        Map<java.time.DayOfWeek, Long> anxiousByDay = moodEntries
            .stream()
            .filter(entry -> entry.getMood() == MoodType.ANXIOUS)
            .collect(Collectors.groupingBy(entry -> entry.getDate().getDayOfWeek(), Collectors.counting()));

        anxiousByDay
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() >= 2)
            .forEach(entry -> {
                insightsList.add("You tend to feel anxious on " + entry.getKey().toString().toLowerCase() + "s");
                recommendations.add("Consider planning relaxing activities for " + entry.getKey().toString().toLowerCase() + "s");
            });

        // Check overall mood balance
        long happyCount = moodEntries.stream().filter(e -> e.getMood() == MoodType.HAPPY).count();
        long sadCount = moodEntries.stream().filter(e -> e.getMood() == MoodType.SAD).count();
        long anxiousCount = moodEntries.stream().filter(e -> e.getMood() == MoodType.ANXIOUS).count();

        if (sadCount > happyCount) {
            insightsList.add("You've had more sad days than happy days recently");
            recommendations.add("Try to engage in activities that bring you joy");
        }

        if (anxiousCount > moodEntries.size() * 0.3) {
            insightsList.add("You've been feeling anxious frequently");
            recommendations.add("Consider practicing mindfulness or meditation");
        }

        insights.put("insights", insightsList);
        insights.put("recommendations", recommendations);

        return insights;
    }

    /**
     * Generate a mood summary report.
     *
     * @param startPeriod the start period
     * @param endPeriod the end period
     * @return the mood summary report
     */
    @Transactional(readOnly = true)
    public String generateMoodSummaryReport(String startPeriod, String endPeriod) {
        LOG.debug("Request to generate mood summary report from {} to {}", startPeriod, endPeriod);

        MoodStatisticsDTO startStats = getMoodStatisticsForMonth(startPeriod);
        MoodStatisticsDTO endStats = getMoodStatisticsForMonth(endPeriod);

        StringBuilder report = new StringBuilder();
        report.append("Mood Summary Report\n");
        report.append("===================\n\n");
        report.append("Period: ").append(startPeriod).append(" to ").append(endPeriod).append("\n\n");

        report.append("Start Period Statistics:\n");
        report.append("- Total entries: ").append(startStats.getTotalEntries()).append("\n");
        report.append("- Happy days: ").append(startStats.getHappyCount()).append("\n");
        report.append("- Sad days: ").append(startStats.getSadCount()).append("\n");
        report.append("- Anxious days: ").append(startStats.getAnxiousCount()).append("\n\n");

        report.append("End Period Statistics:\n");
        report.append("- Total entries: ").append(endStats.getTotalEntries()).append("\n");
        report.append("- Happy days: ").append(endStats.getHappyCount()).append("\n");
        report.append("- Sad days: ").append(endStats.getSadCount()).append("\n");
        report.append("- Anxious days: ").append(endStats.getAnxiousCount()).append("\n\n");

        // Add trends and patterns
        report.append("Trends and Patterns:\n");
        if (endStats.getHappyCount() > startStats.getHappyCount()) {
            report.append("- Improvement in mood: More happy days in the end period\n");
        } else if (endStats.getHappyCount() < startStats.getHappyCount()) {
            report.append("- Decline in mood: Fewer happy days in the end period\n");
        }

        if (endStats.getAnxiousCount() > startStats.getAnxiousCount()) {
            report.append("- Increased anxiety levels\n");
        } else if (endStats.getAnxiousCount() < startStats.getAnxiousCount()) {
            report.append("- Decreased anxiety levels\n");
        }

        // Add general trends section
        report.append("\nGeneral Trends:\n");
        report.append("- Overall mood patterns and trends\n");
        report.append("- Weekly and monthly patterns\n");

        return report.toString();
    }

    /**
     * Calculate the longest streak for a specific mood type.
     */
    private int calculateLongestStreak(List<MoodEntry> moodEntries, MoodType moodType) {
        int longestStreak = 0;
        int currentStreak = 0;

        for (MoodEntry entry : moodEntries) {
            if (entry.getMood() == moodType) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return longestStreak;
    }
}
