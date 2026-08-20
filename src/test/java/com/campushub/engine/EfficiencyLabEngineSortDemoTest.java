package com.campushub.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EfficiencyLabEngineSortDemoTest {

    @Test
    public void runSortDemoReturnsAlgorithmTimingPayload() {
        EfficiencyLabEngine engine = new EfficiencyLabEngine();

        String json = engine.runSortDemo("merge", 100);

        assertNotNull(json);
        assertTrue(json.contains("\"algorithm\":\"MergeSort\""));
        assertTrue(json.contains("\"inputSize\":100"));
        assertTrue(json.contains("\"timeMs\""));
    }
}
