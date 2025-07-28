package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.MoodType;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * A DTO for the MoodTrend entity.
 */
public class MoodTrendDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private List<MoodTrendDataPoint> dataPoints;
    private String period;
    private Integer totalDays;

    public static class MoodTrendDataPoint implements Serializable {

        private LocalDate date;
        private MoodType mood;
        private Integer dayNumber;

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
        }

        public Integer getDayNumber() {
            return dayNumber;
        }

        public void setDayNumber(Integer dayNumber) {
            this.dayNumber = dayNumber;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<MoodTrendDataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<MoodTrendDataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoodTrendDTO)) {
            return false;
        }

        return getId() != null && getId().equals(((MoodTrendDTO) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodTrendDTO{" +
            "id=" + getId() +
            ", dataPoints=" + getDataPoints() +
            ", period='" + getPeriod() + "'" +
            ", totalDays=" + getTotalDays() +
            "}";
    }
}
