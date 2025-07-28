package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * A DTO for the ReminderPreferences entity.
 */
public class ReminderPreferencesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Boolean dailyReminderEnabled;
    private LocalTime dailyReminderTime;
    private Boolean weeklySummaryEnabled;
    private String weeklySummaryDay;
    private String notificationType;
    private String customMessage;
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDailyReminderEnabled() {
        return dailyReminderEnabled;
    }

    public void setDailyReminderEnabled(Boolean dailyReminderEnabled) {
        this.dailyReminderEnabled = dailyReminderEnabled;
    }

    public LocalTime getDailyReminderTime() {
        return dailyReminderTime;
    }

    public void setDailyReminderTime(LocalTime dailyReminderTime) {
        this.dailyReminderTime = dailyReminderTime;
    }

    public Boolean getWeeklySummaryEnabled() {
        return weeklySummaryEnabled;
    }

    public void setWeeklySummaryEnabled(Boolean weeklySummaryEnabled) {
        this.weeklySummaryEnabled = weeklySummaryEnabled;
    }

    public String getWeeklySummaryDay() {
        return weeklySummaryDay;
    }

    public void setWeeklySummaryDay(String weeklySummaryDay) {
        this.weeklySummaryDay = weeklySummaryDay;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
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
        if (!(o instanceof ReminderPreferencesDTO)) {
            return false;
        }

        return getId() != null && getId().equals(((ReminderPreferencesDTO) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReminderPreferencesDTO{" +
            "id=" + getId() +
            ", dailyReminderEnabled='" + getDailyReminderEnabled() + "'" +
            ", dailyReminderTime='" + getDailyReminderTime() + "'" +
            ", weeklySummaryEnabled='" + getWeeklySummaryEnabled() + "'" +
            ", weeklySummaryDay='" + getWeeklySummaryDay() + "'" +
            ", notificationType='" + getNotificationType() + "'" +
            ", customMessage='" + getCustomMessage() + "'" +
            ", userId=" + getUserId() +
            "}";
    }
}
