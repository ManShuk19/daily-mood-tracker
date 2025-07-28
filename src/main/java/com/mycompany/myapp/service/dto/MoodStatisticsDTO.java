package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.MoodType;
import java.io.Serializable;

/**
 * A DTO for the MoodStatistics entity.
 */
public class MoodStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer totalEntries;
    private Integer happyCount;
    private Integer sadCount;
    private Integer angryCount;
    private Integer neutralCount;
    private Integer anxiousCount;
    private MoodType mostCommonMood;
    private String period;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(Integer totalEntries) {
        this.totalEntries = totalEntries;
    }

    public Integer getHappyCount() {
        return happyCount;
    }

    public void setHappyCount(Integer happyCount) {
        this.happyCount = happyCount;
    }

    public Integer getSadCount() {
        return sadCount;
    }

    public void setSadCount(Integer sadCount) {
        this.sadCount = sadCount;
    }

    public Integer getAngryCount() {
        return angryCount;
    }

    public void setAngryCount(Integer angryCount) {
        this.angryCount = angryCount;
    }

    public Integer getNeutralCount() {
        return neutralCount;
    }

    public void setNeutralCount(Integer neutralCount) {
        this.neutralCount = neutralCount;
    }

    public Integer getAnxiousCount() {
        return anxiousCount;
    }

    public void setAnxiousCount(Integer anxiousCount) {
        this.anxiousCount = anxiousCount;
    }

    public MoodType getMostCommonMood() {
        return mostCommonMood;
    }

    public void setMostCommonMood(MoodType mostCommonMood) {
        this.mostCommonMood = mostCommonMood;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoodStatisticsDTO)) {
            return false;
        }

        return getId() != null && getId().equals(((MoodStatisticsDTO) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodStatisticsDTO{" +
            "id=" + getId() +
            ", totalEntries=" + getTotalEntries() +
            ", happyCount=" + getHappyCount() +
            ", sadCount=" + getSadCount() +
            ", angryCount=" + getAngryCount() +
            ", neutralCount=" + getNeutralCount() +
            ", anxiousCount=" + getAnxiousCount() +
            ", mostCommonMood='" + getMostCommonMood() + "'" +
            ", period='" + getPeriod() + "'" +
            "}";
    }
}
