package com.synopsys.integration.detect.lifecycle.shutdown;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.configuration.util.ConfigTestUtils;
import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;

public class ShutdownDeciderTest {
    @Test
    public void defaultDoesNotPreserve() {
        CleanupDecision decision = new ShutdownDecider().decideCleanup(ConfigTestUtils.emptyConfig(), null, null);
        assertTrue(decision.shouldCleanup());
        assertFalse(decision.shouldPreserveAirGap());
        assertFalse(decision.shouldPreserveBdio());
        assertFalse(decision.shouldPreserveScan());
    }

    @Test
    public void whenAirGapProvidedItIsPreserved() {
        CleanupDecision decision = new ShutdownDecider().decideCleanup(ConfigTestUtils.emptyConfig(), null, new File("."));
        assertTrue(decision.shouldPreserveAirGap());
    }

    @Test
    public void shouldPreserveBdioAndScanIfOffline() {
        ProductRunData productRunData = new ProductRunData(BlackDuckRunData.offline(), null);
        CleanupDecision decision = new ShutdownDecider().decideCleanup(ConfigTestUtils.emptyConfig(), productRunData, null);
        assertTrue(decision.shouldPreserveBdio());
        assertTrue(decision.shouldPreserveScan());
    }

    @Test
    public void shouldPreserveScanIfDryRun() {
        PropertyConfiguration configuration = ConfigTestUtils.configOf(Pair.of(DetectProperties.DETECT_BLACKDUCK_SIGNATURE_SCANNER_DRY_RUN.getProperty().getKey(), "true"));
        CleanupDecision decision = new ShutdownDecider().decideCleanup(configuration, null, null);
        assertTrue(decision.shouldPreserveScan());
    }

    @Test
    public void shouldSkipCleanupIfProvidedFalse() {
        PropertyConfiguration configuration = ConfigTestUtils.configOf(Pair.of(DetectProperties.DETECT_CLEANUP.getProperty().getKey(), "false"));
        CleanupDecision decision = new ShutdownDecider().decideCleanup(configuration, null, null);
        assertFalse(decision.shouldCleanup());
    }
}
