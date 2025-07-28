package com.mycompany.myapp.service.dto;

import java.io.Serializable;

/**
 * A DTO for the MoodGoal entity.
 */
public class MoodGoalDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String type;
    private Integer target;
    private String timeframe;
    private String description;
    private Boolean active;
    private Boolean completed;
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

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
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
        if (!(o instanceof MoodGoalDTO)) {
            return false;
        }

        return getId() != null && getId().equals(((MoodGoalDTO) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodGoalDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", target=" + getTarget() +
            ", timeframe='" + getTimeframe() + "'" +
            ", description='" + getDescription() + "'" +
            ", active='" + getActive() + "'" +
            ", completed='" + getCompleted() + "'" +
            ", userId=" + getUserId() +
            "}";
    }
}
