package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.MoodType;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A DTO for mood statistics.
 */
public class MoodStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalEntries;
    private Map<MoodType, Long> moodDistribution;
    private MoodType mostFrequentMood;
    private Long mostFrequentMoodCount;
    private Double averageMoodScore;
    private List<MoodTrendDTO> trends;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long currentStreak;
    private MoodType currentStreakMood;
    private Long longestStreak;
    private MoodType longestStreakMood;
    private Double trackingCompletionRate;

    public MoodStatisticsDTO() {
        // Empty constructor needed for Jackson.
    }

    public Long getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(Long totalEntries) {
        this.totalEntries = totalEntries;
    }

    public Map<MoodType, Long> getMoodDistribution() {
        return moodDistribution;
    }

    public void setMoodDistribution(Map<MoodType, Long> moodDistribution) {
        this.moodDistribution = moodDistribution;
    }

    public MoodType getMostFrequentMood() {
        return mostFrequentMood;
    }

    public void setMostFrequentMood(MoodType mostFrequentMood) {
        this.mostFrequentMood = mostFrequentMood;
    }

    public Long getMostFrequentMoodCount() {
        return mostFrequentMoodCount;
    }

    public void setMostFrequentMoodCount(Long mostFrequentMoodCount) {
        this.mostFrequentMoodCount = mostFrequentMoodCount;
    }

    public Double getAverageMoodScore() {
        return averageMoodScore;
    }

    public void setAverageMoodScore(Double averageMoodScore) {
        this.averageMoodScore = averageMoodScore;
    }

    public List<MoodTrendDTO> getTrends() {
        return trends;
    }

    public void setTrends(List<MoodTrendDTO> trends) {
        this.trends = trends;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Long currentStreak) {
        this.currentStreak = currentStreak;
    }

    public MoodType getCurrentStreakMood() {
        return currentStreakMood;
    }

    public void setCurrentStreakMood(MoodType currentStreakMood) {
        this.currentStreakMood = currentStreakMood;
    }

    public Long getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Long longestStreak) {
        this.longestStreak = longestStreak;
    }

    public MoodType getLongestStreakMood() {
        return longestStreakMood;
    }

    public void setLongestStreakMood(MoodType longestStreakMood) {
        this.longestStreakMood = longestStreakMood;
    }

    public Double getTrackingCompletionRate() {
        return trackingCompletionRate;
    }

    public void setTrackingCompletionRate(Double trackingCompletionRate) {
        this.trackingCompletionRate = trackingCompletionRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoodStatisticsDTO)) {
            return false;
        }

        MoodStatisticsDTO moodStatisticsDTO = (MoodStatisticsDTO) o;
        if (this.totalEntries == null) {
            return false;
        }
        return totalEntries.equals(moodStatisticsDTO.totalEntries);
    }

    @Override
    public int hashCode() {
        return totalEntries.hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodStatisticsDTO{" +
            "totalEntries=" + getTotalEntries() +
            ", moodDistribution='" + getMoodDistribution() + "'" +
            ", mostFrequentMood='" + getMostFrequentMood() + "'" +
            ", mostFrequentMoodCount=" + getMostFrequentMoodCount() +
            ", averageMoodScore=" + getAverageMoodScore() +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", currentStreak=" + getCurrentStreak() +
            ", currentStreakMood='" + getCurrentStreakMood() + "'" +
            ", longestStreak=" + getLongestStreak() +
            ", longestStreakMood='" + getLongestStreakMood() + "'" +
            ", trackingCompletionRate=" + getTrackingCompletionRate() +
            "}";
    }
}
