package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.MoodEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MoodEntry entity.
 */
@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {
    @Query("select moodEntry from MoodEntry moodEntry where moodEntry.user.login = ?#{authentication.name}")
    List<MoodEntry> findByUserIsCurrentUser();

    default Optional<MoodEntry> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<MoodEntry> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<MoodEntry> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select moodEntry from MoodEntry moodEntry left join fetch moodEntry.user",
        countQuery = "select count(moodEntry) from MoodEntry moodEntry"
    )
    Page<MoodEntry> findAllWithToOneRelationships(Pageable pageable);

    @Query("select moodEntry from MoodEntry moodEntry left join fetch moodEntry.user")
    List<MoodEntry> findAllWithToOneRelationships();

    @Query("select moodEntry from MoodEntry moodEntry left join fetch moodEntry.user where moodEntry.id =:id")
    Optional<MoodEntry> findOneWithToOneRelationships(@Param("id") Long id);
}
