package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.MoodEntryAsserts.*;
import static com.mycompany.myapp.domain.MoodEntryTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoodEntryMapperTest {

    private MoodEntryMapper moodEntryMapper;

    @BeforeEach
    void setUp() {
        moodEntryMapper = new MoodEntryMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMoodEntrySample1();
        var actual = moodEntryMapper.toEntity(moodEntryMapper.toDto(expected));
        assertMoodEntryAllPropertiesEquals(expected, actual);
    }
}
