/*
Write secure code to schedule a system task that safely restricts the scheduler to only trigger pre-compiled, authorized internal functions
*/

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A task scheduler restricted to a fixed, compile-time whitelist of
 * authorized internal functions. Tasks cannot be added, replaced, or
 * invoked dynamically at runtime — only the enum constants below can
 * ever be scheduled.
 */
public final class SecureTaskScheduler {

    private static final Logger LOG = Logger.getLogger(SecureTaskScheduler.class.getName());

    /**
     * The full set of tasks this scheduler is ever allowed to run.
     * Adding a new task requires a code change + recompile + review —
     * not a runtime registration call.
     */
    public enum AuthorizedTask {
        CLEANUP_TEMP_FILES(SecureTaskScheduler::cleanupTempFiles),
        REFRESH_CACHE(SecureTaskScheduler::refreshCache),
        HEALTH_CHECK(SecureTaskScheduler::healthCheck);

        private final Runnable action;

        AuthorizedTask(Runnable action) {
            this.action = action;
        }
    }

    // Immutable, built once at class-load time from the enum itself —
    // there is no code path that inserts anything else into this map.
    private static final Map<AuthorizedTask, Runnable> REGISTRY;
    static {
        Map<AuthorizedTask, Runnable> m = new EnumMap<>(AuthorizedTask.class);
        for (AuthorizedTask t : AuthorizedTask.values()) {
            m.put(t, t.action);
        }
        REGISTRY = Map.copyOf(m); // unmodifiable
    }

    private final ScheduledExecutorService executor;

    public SecureTaskScheduler(int threadPoolSize) {
        if (threadPoolSize < 1) {
            throw new IllegalArgumentException("threadPoolSize must be >= 1");
        }
        this.executor = Executors.newScheduledThreadPool(
                threadPoolSize,
                r -> {
                    Thread t = new Thread(r, "secure-scheduler");
                    t.setDaemon(true);
                    // Uncaught exceptions are still logged, never silently swallowed
                    t.setUncaughtExceptionHandler((thread, ex) ->
                            LOG.log(Level.SEVERE, "Uncaught error in " + thread.getName(), ex));
                    return t;
                }
        );
    }

    /**
     * Schedule an authorized task to run repeatedly at a fixed rate.
     * The only valid argument type is AuthorizedTask — there is no
     * overload that accepts a String, Class name, or Runnable, so
     * arbitrary code can never be scheduled through this API.
     */
    public ScheduledFuture<?> scheduleAtFixedRate(AuthorizedTask task, Duration initialDelay, Duration period) {
        requireKnownTask(task);
        Runnable safeWrapper = wrapSafely(task);
        return executor.scheduleAtFixedRate(
                safeWrapper,
                initialDelay.toMillis(),
                period.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    /** Schedule an authorized task to run once, after a delay. */
    public ScheduledFuture<?> scheduleOnce(AuthorizedTask task, Duration delay) {
        requireKnownTask(task);
        Runnable safeWrapper = wrapSafely(task);
        return executor.schedule(safeWrapper, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void requireKnownTask(AuthorizedTask task) {
        // Defense in depth: even though the type system already restricts
        // this, explicitly verify the task exists in the registry and
        // reject null.
        if (task == null || !REGISTRY.containsKey(task)) {
            throw new SecurityException("Rejected: task is not in the authorized registry");
        }
    }

    private Runnable wrapSafely(AuthorizedTask task) {
        return () -> {
            Instant start = Instant.now();
            LOG.info(() -> "Starting authorized task: " + task);
            try {
                REGISTRY.get(task).run();
                LOG.info(() -> "Completed task: " + task + " in " +
                        Duration.between(start, Instant.now()).toMillis() + "ms");
            } catch (Throwable t) {
                // Contain failures so one task can never crash the pool
                // or block subsequent scheduled runs.
                LOG.log(Level.SEVERE, "Task failed: " + task, t);
            }
        };
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ---- Actual authorized internal functions ----
    // Private, pre-compiled, and only reachable via the enum above.

    private static void cleanupTempFiles() {
        // Real implementation here
        LOG.fine("Cleaning up temp files...");
    }

    private static void refreshCache() {
        LOG.fine("Refreshing cache...");
    }

    private static void healthCheck() {
        LOG.fine("Running health check...");
    }

    // ---- Example usage ----
    public static void main(String[] args) throws InterruptedException {
        SecureTaskScheduler scheduler = new SecureTaskScheduler(2);

        scheduler.scheduleAtFixedRate(
                AuthorizedTask.HEALTH_CHECK,
                Duration.ofSeconds(0),
                Duration.ofSeconds(30)
        );

        scheduler.scheduleOnce(AuthorizedTask.CLEANUP_TEMP_FILES, Duration.ofMinutes(5));

        // Demonstrates rejection: this line would not even compile,
        // since only AuthorizedTask values are accepted:
        // scheduler.scheduleOnce("rm -rf /", Duration.ofSeconds(1)); // compile error

        Thread.sleep(2000);
        scheduler.shutdown();
    }
}