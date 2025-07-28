package com.mycompany.myapp.service.dto;

import java.io.Serializable;

/**
 * A DTO for the GoalProgress entity.
 */
public class GoalProgressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer completed;
    private Integer target;
    private Integer percentage;
    private Integer currentStreak;
    private String goalType;
    private Long goalId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
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
        if (!(o instanceof GoalProgressDTO)) {
            return false;
        }

        return getId() != null && getId().equals(((GoalProgressDTO) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GoalProgressDTO{" +
            "id=" + getId() +
            ", completed=" + getCompleted() +
            ", target=" + getTarget() +
            ", percentage=" + getPercentage() +
            ", currentStreak=" + getCurrentStreak() +
            ", goalType='" + getGoalType() + "'" +
            ", goalId=" + getGoalId() +
            "}";
    }
}
