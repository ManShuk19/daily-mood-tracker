package com.mycompany.myapp.domain;

import com.mycompany.myapp.domain.enumeration.MoodType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A MoodEntry.
 */
@Entity
@Table(name = "mood_entry", uniqueConstraints = { @UniqueConstraint(columnNames = { "date", "user_id" }) })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MoodEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "mood", nullable = false)
    private MoodType mood;

    @ManyToOne(optional = false)
    @NotNull
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MoodEntry id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public MoodEntry date(LocalDate date) {
        this.setDate(date);
        return this;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public MoodType getMood() {
        return this.mood;
    }

    public MoodEntry mood(MoodType mood) {
        this.setMood(mood);
        return this;
    }

    public void setMood(MoodType mood) {
        this.mood = mood;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MoodEntry user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoodEntry)) {
            return false;
        }
        return getId() != null && getId().equals(((MoodEntry) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoodEntry{" +
            "id=" + getId() +
            ", date='" + getDate() + "'" +
            ", mood='" + getMood() + "'" +
            "}";
    }
}
