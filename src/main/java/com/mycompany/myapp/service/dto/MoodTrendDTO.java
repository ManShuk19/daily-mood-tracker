package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.MoodType;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A DTO for mood trends.
 */
public class MoodTrendDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private MoodType mood;
    private Integer moodScore;

    public MoodTrendDTO() {
        // Empty constructor needed for Jackson.
    }

    public MoodTrendDTO(LocalDate date, MoodType mood) {
        this.date = date;
        this.mood = mood;
        this.moodScore = getMoodScore(mood);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public MoodType getMood() {
        return mood;
    }

    public void setMood(MoodType mood) {
        this.mood = mood;
        this.moodScore = getMoodScore(mood);
    }

    public Integer getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }

    private Integer getMoodScore(MoodType mood) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoodTrendDTO)) {
            return false;
        }

        MoodTrendDTO moodTrendDTO = (MoodTrendDTO) o;
        if (this.date == null) {
            return false;
        }
        return date.equals(moodTrendDTO.date);
    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodTrendDTO{" +
            "date='" + getDate() + "'" +
            ", mood='" + getMood() + "'" +
            ", moodScore=" + getMoodScore() +
            "}";
    }
}
