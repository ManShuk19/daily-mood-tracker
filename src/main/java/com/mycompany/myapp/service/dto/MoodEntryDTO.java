package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.MoodType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.MoodEntry} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MoodEntryDTO implements Serializable {

    private Long id;

    @NotNull
    private LocalDate date;

    @NotNull
    private MoodType mood;

    @NotNull
    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoodEntryDTO)) {
            return false;
        }

        MoodEntryDTO moodEntryDTO = (MoodEntryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, moodEntryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodEntryDTO{" +
            "id=" + getId() +
            ", date='" + getDate() + "'" +
            ", mood='" + getMood() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
