package com.example.common;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runners.model.Statement;

public abstract class BaseTest {

    @BeforeClass
    public static void ensureServerStarted() {
        LocalTestServer.startServer();
    }

    @Rule
    public TestRule timeoutRule = (base, description) -> {
        long timeoutSecs = getTimeoutSeconds();
        TimeoutSeconds annotation = description.getAnnotation(TimeoutSeconds.class);
        if (annotation != null) {
            timeoutSecs = annotation.value();
        }
        return Timeout.seconds(timeoutSecs).apply(base, description);
    };

    @Rule
    public TestRule performanceWarning = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            long start = System.currentTimeMillis();
            try {
                base.evaluate();
            } finally {
                long duration = System.currentTimeMillis() - start;
                long optimalMs = getOptimalTimeoutMillis();

                OptimalTimeoutMillis annotation = description.getAnnotation(OptimalTimeoutMillis.class);
                if (annotation != null) {
                    optimalMs = annotation.value();
                }

                if (duration > optimalMs) {
                    System.err.printf("PERFORMANCE WARNING: %s.%s took %d ms (Optimal: < %d ms)%n",
                        description.getClassName(), description.getMethodName(), duration, optimalMs);
                }
            }
        }
    };

    protected long getTimeoutSeconds() {
        return 10;
    }

    protected long getOptimalTimeoutMillis() {
        return 100;
    }
}
