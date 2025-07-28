package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * A DTO for the MoodReminder entity.
 */
public class MoodReminderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String type;
    private LocalTime reminderTime;
    private String dayOfWeek;
    private Boolean enabled;
    private Integer streakTarget;
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getStreakTarget() {
        return streakTarget;
    }

    public void setStreakTarget(Integer streakTarget) {
        this.streakTarget = streakTarget;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoodReminderDTO)) {
            return false;
        }

        return getId() != null && getId().equals(((MoodReminderDTO) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodReminderDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", reminderTime='" + getReminderTime() + "'" +
            ", dayOfWeek='" + getDayOfWeek() + "'" +
            ", enabled='" + getEnabled() + "'" +
            ", streakTarget=" + getStreakTarget() +
            ", userId=" + getUserId() +
            "}";
    }
}
