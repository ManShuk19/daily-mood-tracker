package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.service.mapper.MoodEntryMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.MoodEntry}.
 */
@Service
@Transactional
public class MoodEntryService {

    private static final Logger LOG = LoggerFactory.getLogger(MoodEntryService.class);

    private final MoodEntryRepository moodEntryRepository;

    private final MoodEntryMapper moodEntryMapper;

    private final UserRepository userRepository;

    public MoodEntryService(MoodEntryRepository moodEntryRepository, MoodEntryMapper moodEntryMapper, UserRepository userRepository) {
        this.moodEntryRepository = moodEntryRepository;
        this.moodEntryMapper = moodEntryMapper;
        this.userRepository = userRepository;
    }

    /**
     * Save a moodEntry.
     *
     * @param moodEntryDTO the entity to save.
     * @return the persisted entity.
     */
    public MoodEntryDTO save(MoodEntryDTO moodEntryDTO) {
        LOG.debug("Request to save MoodEntry : {}", moodEntryDTO);
        MoodEntry moodEntry = moodEntryMapper.toEntity(moodEntryDTO);

        // Automatically assign the current user to the mood entry
        SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).ifPresent(moodEntry::setUser);

        moodEntry = moodEntryRepository.save(moodEntry);
        return moodEntryMapper.toDto(moodEntry);
    }

    /**
     * Update a moodEntry.
     *
     * @param moodEntryDTO the entity to save.
     * @return the persisted entity.
     */
    public MoodEntryDTO update(MoodEntryDTO moodEntryDTO) {
        LOG.debug("Request to update MoodEntry : {}", moodEntryDTO);
        MoodEntry moodEntry = moodEntryMapper.toEntity(moodEntryDTO);
        moodEntry = moodEntryRepository.save(moodEntry);
        return moodEntryMapper.toDto(moodEntry);
    }

    /**
     * Partially update a moodEntry.
     *
     * @param moodEntryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MoodEntryDTO> partialUpdate(MoodEntryDTO moodEntryDTO) {
        LOG.debug("Request to partially update MoodEntry : {}", moodEntryDTO);

        return moodEntryRepository
            .findById(moodEntryDTO.getId())
            .map(existingMoodEntry -> {
                moodEntryMapper.partialUpdate(existingMoodEntry, moodEntryDTO);

                return existingMoodEntry;
            })
            .map(moodEntryRepository::save)
            .map(moodEntryMapper::toDto);
    }

    /**
     * Get all the moodEntries.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MoodEntryDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all MoodEntries");
        return moodEntryRepository.findAll(pageable).map(moodEntryMapper::toDto);
    }

    /**
     * Get all the moodEntries with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<MoodEntryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return moodEntryRepository.findAllWithEagerRelationships(pageable).map(moodEntryMapper::toDto);
    }

    /**
     * Get one moodEntry by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MoodEntryDTO> findOne(Long id) {
        LOG.debug("Request to get MoodEntry : {}", id);
        return moodEntryRepository.findOneWithEagerRelationships(id).map(moodEntryMapper::toDto);
    }

    /**
     * Delete the moodEntry by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete MoodEntry : {}", id);
        moodEntryRepository.deleteById(id);
    }

    /**
     * Get all mood entries for the current user.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MoodEntryDTO> findAllForCurrentUser(Pageable pageable) {
        LOG.debug("Request to get all MoodEntries for current user");
        List<MoodEntry> moodEntries = moodEntryRepository.findByUserIsCurrentUser();
        List<MoodEntryDTO> dtos = moodEntries.stream().map(moodEntryMapper::toDto).toList();

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<MoodEntryDTO> pagedDtos = dtos.subList(start, Math.min(start + pageable.getPageSize(), dtos.size()));

        return new PageImpl<>(pagedDtos, pageable, dtos.size());
    }

    /**
     * Get all mood entries for the current user (no pagination).
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<MoodEntryDTO> findAllForCurrentUser() {
        LOG.debug("Request to get all MoodEntries for current user");
        return moodEntryRepository.findByUserIsCurrentUser().stream().map(moodEntryMapper::toDto).toList();
    }
}
