package com.example.common;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.lang.reflect.Method;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

@ExtendWith(BaseTest.DynamicTimeoutExtension.class)
public abstract class BaseTest {

    private long startTime;

    public static class DynamicTimeoutExtension implements InvocationInterceptor {
        @Override
        public void interceptTestMethod(Invocation<Void> invocation,
                                        ReflectiveInvocationContext<Method> invocationContext,
                                        ExtensionContext extensionContext) throws Throwable {
            Object testInstance = extensionContext.getRequiredTestInstance();
            if (testInstance instanceof BaseTest baseTest) {
                long seconds = baseTest.getTimeoutSeconds();
                
                // Check for method-specific hard timeout override
                Method testMethod = invocationContext.getExecutable();
                TimeoutSeconds annotation = testMethod.getAnnotation(TimeoutSeconds.class);
                if (annotation != null) {
                    seconds = annotation.value();
                }

                assertTimeoutPreemptively(Duration.ofSeconds(seconds), invocation::proceed);
            } else {
                invocation.proceed();
            }
        }
    }

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
        java.util.concurrent.atomic.AtomicLong optimalMs = new java.util.concurrent.atomic.AtomicLong(getOptimalTimeoutMillis());

        // Check for custom optimal timeout annotation on method
        testInfo.getTestMethod().ifPresent(method -> {
            OptimalTimeoutMillis annotation = method.getAnnotation(OptimalTimeoutMillis.class);
            if (annotation != null) {
                optimalMs.set(annotation.value());
            }
        });

        if (duration > optimalMs.get()) {
            System.err.printf("PERFORMANCE WARNING: %s took %d ms (Optimal: < %d ms)%n",
                testInfo.getDisplayName(), duration, optimalMs.get());
        }
    }

    protected long getTimeoutSeconds() {
        return 10;
    }

    protected long getOptimalTimeoutMillis() {
        return 100;
    }
}
