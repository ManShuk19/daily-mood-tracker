package com.mycompany.myapp.service.dto;

import java.io.Serializable;

/**
 * A DTO for the GoalAchievement entity.
 */
public class GoalAchievementDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Boolean achieved;
    private Boolean goalCompleted;
    private Integer pointsEarned;
    private String achievementMessage;
    private Long goalId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getAchieved() {
        return achieved;
    }

    public void setAchieved(Boolean achieved) {
        this.achieved = achieved;
    }

    public Boolean getGoalCompleted() {
        return goalCompleted;
    }

    public void setGoalCompleted(Boolean goalCompleted) {
        this.goalCompleted = goalCompleted;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public String getAchievementMessage() {
        return achievementMessage;
    }

    public void setAchievementMessage(String achievementMessage) {
        this.achievementMessage = achievementMessage;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GoalAchievementDTO)) {
            return false;
        }

        return getId() != null && getId().equals(((GoalAchievementDTO) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GoalAchievementDTO{" +
            "id=" + getId() +
            ", achieved='" + getAchieved() + "'" +
            ", goalCompleted='" + getGoalCompleted() + "'" +
            ", pointsEarned=" + getPointsEarned() +
            ", achievementMessage='" + getAchievementMessage() + "'" +
            ", goalId=" + getGoalId() +
            "}";
    }
}
