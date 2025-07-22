package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class MoodEntryTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static MoodEntry getMoodEntrySample1() {
        return new MoodEntry().id(1L);
    }

    public static MoodEntry getMoodEntrySample2() {
        return new MoodEntry().id(2L);
    }

    public static MoodEntry getMoodEntryRandomSampleGenerator() {
        return new MoodEntry().id(longCount.incrementAndGet());
    }
}
