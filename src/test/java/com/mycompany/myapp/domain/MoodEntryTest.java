package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.MoodEntryTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MoodEntryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MoodEntry.class);
        MoodEntry moodEntry1 = getMoodEntrySample1();
        MoodEntry moodEntry2 = new MoodEntry();
        assertThat(moodEntry1).isNotEqualTo(moodEntry2);

        moodEntry2.setId(moodEntry1.getId());
        assertThat(moodEntry1).isEqualTo(moodEntry2);

        moodEntry2 = getMoodEntrySample2();
        assertThat(moodEntry1).isNotEqualTo(moodEntry2);
    }
}
