package com.example.GameLogic;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class BaseTest {
    
    @Rule
    public Timeout globalTimeout() {
        return Timeout.seconds(getTimeoutSeconds());
    }

    @Rule
    public TestRule performanceWarning() {
        return new TestRule() {
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
    }

    /**
     * Hard limit: Test fails if it exceeds this.
     * Default: 10 seconds.
     */
    protected long getTimeoutSeconds() {
        return 10;
    }

    /**
     * Soft limit: Test warns if it exceeds this.
     * Default: 100 milliseconds.
     */
    protected long getOptimalTimeoutMillis() {
        return 100;
    }
}