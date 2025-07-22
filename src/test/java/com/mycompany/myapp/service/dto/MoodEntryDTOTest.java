package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MoodEntryDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MoodEntryDTO.class);
        MoodEntryDTO moodEntryDTO1 = new MoodEntryDTO();
        moodEntryDTO1.setId(1L);
        MoodEntryDTO moodEntryDTO2 = new MoodEntryDTO();
        assertThat(moodEntryDTO1).isNotEqualTo(moodEntryDTO2);
        moodEntryDTO2.setId(moodEntryDTO1.getId());
        assertThat(moodEntryDTO1).isEqualTo(moodEntryDTO2);
        moodEntryDTO2.setId(2L);
        assertThat(moodEntryDTO1).isNotEqualTo(moodEntryDTO2);
        moodEntryDTO1.setId(null);
        assertThat(moodEntryDTO1).isNotEqualTo(moodEntryDTO2);
    }
}
