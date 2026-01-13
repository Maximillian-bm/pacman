package com.example.common;

import com.example.GameLogic.LocalTestServer;
import com.example.GameLogic.OptimalTimeoutMillis;
import com.example.GameLogic.TimeoutSeconds;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class BaseTest {

    @BeforeClass
    public static void ensureServerStarted() {
        LocalTestServer.startServer();
    }
    
    @Rule
    public TestRule timeoutRule = new TestRule() {
        @Override
        public Statement apply(Statement base, Description description) {
            long timeoutSecs = getTimeoutSeconds();
            TimeoutSeconds annotation = description.getAnnotation(TimeoutSeconds.class);
            if (annotation != null) {
                timeoutSecs = annotation.value();
            }
            return Timeout.seconds(timeoutSecs).apply(base, description);
        }
    };

    @Rule
    public TestRule performanceWarning = new TestRule() {
        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
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

                        // Only warn if the test finished (didn't timeout hard) but was slow
                        if (duration > optimalMs) {
                            System.err.printf("PERFORMANCE WARNING: %s.%s took %d ms (Optimal: < %d ms)%n",
                                    description.getClassName(), description.getMethodName(), duration, optimalMs);
                        }
                    }
                }
            };
        }
    };

    /**
     * Hard limit: Test fails if it exceeds this.
     * Default: 10 seconds.
     * Override per class or use @TimeoutSeconds per method.
     */
    protected long getTimeoutSeconds() {
        return 10;
    }

    /**
     * Soft limit: Test warns if it exceeds this.
     * Default: 100 milliseconds.
     * Override per class or use @OptimalTimeoutMillis per method.
     */
    protected long getOptimalTimeoutMillis() {
        return 100;
    }
}
