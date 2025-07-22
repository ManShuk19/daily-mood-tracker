package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.MoodEntryRepository;
import com.mycompany.myapp.service.MoodEntryService;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.MoodEntry}.
 */
@RestController
@RequestMapping("/api/mood-entries")
public class MoodEntryResource {

    private static final Logger LOG = LoggerFactory.getLogger(MoodEntryResource.class);

    private static final String ENTITY_NAME = "moodEntry";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MoodEntryService moodEntryService;

    private final MoodEntryRepository moodEntryRepository;

    public MoodEntryResource(MoodEntryService moodEntryService, MoodEntryRepository moodEntryRepository) {
        this.moodEntryService = moodEntryService;
        this.moodEntryRepository = moodEntryRepository;
    }

    /**
     * {@code POST  /mood-entries} : Create a new moodEntry.
     *
     * @param moodEntryDTO the moodEntryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new moodEntryDTO, or with status {@code 400 (Bad Request)} if the moodEntry has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MoodEntryDTO> createMoodEntry(@Valid @RequestBody MoodEntryDTO moodEntryDTO) throws URISyntaxException {
        LOG.debug("REST request to save MoodEntry : {}", moodEntryDTO);
        if (moodEntryDTO.getId() != null) {
            throw new BadRequestAlertException("A new moodEntry cannot already have an ID", ENTITY_NAME, "idexists");
        }
        moodEntryDTO = moodEntryService.save(moodEntryDTO);
        return ResponseEntity.created(new URI("/api/mood-entries/" + moodEntryDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, moodEntryDTO.getId().toString()))
            .body(moodEntryDTO);
    }

    /**
     * {@code PUT  /mood-entries/:id} : Updates an existing moodEntry.
     *
     * @param id the id of the moodEntryDTO to save.
     * @param moodEntryDTO the moodEntryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated moodEntryDTO,
     * or with status {@code 400 (Bad Request)} if the moodEntryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the moodEntryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MoodEntryDTO> updateMoodEntry(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MoodEntryDTO moodEntryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MoodEntry : {}, {}", id, moodEntryDTO);
        if (moodEntryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, moodEntryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!moodEntryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        moodEntryDTO = moodEntryService.update(moodEntryDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, moodEntryDTO.getId().toString()))
            .body(moodEntryDTO);
    }

    /**
     * {@code PATCH  /mood-entries/:id} : Partial updates given fields of an existing moodEntry, field will ignore if it is null
     *
     * @param id the id of the moodEntryDTO to save.
     * @param moodEntryDTO the moodEntryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated moodEntryDTO,
     * or with status {@code 400 (Bad Request)} if the moodEntryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the moodEntryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the moodEntryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MoodEntryDTO> partialUpdateMoodEntry(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MoodEntryDTO moodEntryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MoodEntry partially : {}, {}", id, moodEntryDTO);
        if (moodEntryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, moodEntryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!moodEntryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MoodEntryDTO> result = moodEntryService.partialUpdate(moodEntryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, moodEntryDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /mood-entries} : get all the moodEntries.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of moodEntries in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MoodEntryDTO>> getAllMoodEntries(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of MoodEntries");
        Page<MoodEntryDTO> page;
        if (eagerload) {
            page = moodEntryService.findAllWithEagerRelationships(pageable);
        } else {
            page = moodEntryService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /mood-entries/:id} : get the "id" moodEntry.
     *
     * @param id the id of the moodEntryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the moodEntryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MoodEntryDTO> getMoodEntry(@PathVariable("id") Long id) {
        LOG.debug("REST request to get MoodEntry : {}", id);
        Optional<MoodEntryDTO> moodEntryDTO = moodEntryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(moodEntryDTO);
    }

    /**
     * {@code DELETE  /mood-entries/:id} : delete the "id" moodEntry.
     *
     * @param id the id of the moodEntryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMoodEntry(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete MoodEntry : {}", id);
        moodEntryService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
