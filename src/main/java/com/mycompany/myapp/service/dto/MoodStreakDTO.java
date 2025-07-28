package com.mycompany.myapp.service.dto;

import java.io.Serializable;

/**
 * A DTO for the MoodStreak entity.
 */
public class MoodStreakDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer currentHappyStreak;
    private Integer longestHappyStreak;
    private Integer currentSadStreak;
    private Integer longestSadStreak;
    private Integer currentAnxiousStreak;
    private Integer longestAnxiousStreak;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCurrentHappyStreak() {
        return currentHappyStreak;
    }

    public void setCurrentHappyStreak(Integer currentHappyStreak) {
        this.currentHappyStreak = currentHappyStreak;
    }

    public Integer getLongestHappyStreak() {
        return longestHappyStreak;
    }

    public void setLongestHappyStreak(Integer longestHappyStreak) {
        this.longestHappyStreak = longestHappyStreak;
    }

    public Integer getCurrentSadStreak() {
        return currentSadStreak;
    }

    public void setCurrentSadStreak(Integer currentSadStreak) {
        this.currentSadStreak = currentSadStreak;
    }

    public Integer getLongestSadStreak() {
        return longestSadStreak;
    }

    public void setLongestSadStreak(Integer longestSadStreak) {
        this.longestSadStreak = longestSadStreak;
    }

    public Integer getCurrentAnxiousStreak() {
        return currentAnxiousStreak;
    }

    public void setCurrentAnxiousStreak(Integer currentAnxiousStreak) {
        this.currentAnxiousStreak = currentAnxiousStreak;
    }

    public Integer getLongestAnxiousStreak() {
        return longestAnxiousStreak;
    }

    public void setLongestAnxiousStreak(Integer longestAnxiousStreak) {
        this.longestAnxiousStreak = longestAnxiousStreak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoodStreakDTO)) {
            return false;
        }

        return getId() != null && getId().equals(((MoodStreakDTO) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodStreakDTO{" +
            "id=" + getId() +
            ", currentHappyStreak=" + getCurrentHappyStreak() +
            ", longestHappyStreak=" + getLongestHappyStreak() +
            ", currentSadStreak=" + getCurrentSadStreak() +
            ", longestSadStreak=" + getLongestSadStreak() +
            ", currentAnxiousStreak=" + getCurrentAnxiousStreak() +
            ", longestAnxiousStreak=" + getLongestAnxiousStreak() +
            "}";
    }
}
