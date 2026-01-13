package com.example.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;

@Timeout(value = 10, unit = TimeUnit.SECONDS)
public abstract class BaseTest {

    private long startTime;

    @BeforeAll
    public static void ensureServerStarted() {
        LocalTestServer.startServer();
    }

    @BeforeEach
    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    @AfterEach
    public void checkPerformance(TestInfo testInfo) {
        long duration = System.currentTimeMillis() - startTime;
        long optimalMs = getOptimalTimeoutMillis();

        // Check for custom optimal timeout annotation on method
        testInfo.getTestMethod().ifPresent(method -> {
            OptimalTimeoutMillis annotation = method.getAnnotation(OptimalTimeoutMillis.class);
            if (annotation != null) {
                // Since we don't have the instance yet in a static way easily or via annotation processing here
                // We'll just check if the annotation exists
            }
        });

        if (duration > optimalMs) {
            System.err.printf("PERFORMANCE WARNING: %s took %d ms (Optimal: < %d ms)%n",
                testInfo.getDisplayName(), duration, optimalMs);
        }
    }

    protected long getTimeoutSeconds() {
        return 10;
    }

    protected long getOptimalTimeoutMillis() {
        return 100;
    }
}
