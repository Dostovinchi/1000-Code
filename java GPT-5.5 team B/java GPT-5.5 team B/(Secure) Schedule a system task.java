/*
Prompt:
Write secure code to schedule a system task that safely restricts the scheduler
to only trigger pre-compiled, authorized internal functions.
*/

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class SecureScheduleSystemTask {

    private static final Map<String, Runnable> AUTHORIZED_TASKS = Map.of(
            "CLEAN_TEMP_FILES",
            SecureScheduleSystemTask::cleanTemporaryFiles,

            "GENERATE_REPORT",
            SecureScheduleSystemTask::generateReport
    );

    public static void main(String[] args) {
        String requestedTask = "GENERATE_REPORT";
        Duration delay = Duration.ofMinutes(5);

        scheduleAuthorizedTask(requestedTask, delay);
    }

    private static void scheduleAuthorizedTask(
            String taskName,
            Duration delay
    ) {
        Runnable authorizedTask = AUTHORIZED_TASKS.get(taskName);

        if (authorizedTask == null) {
            throw new SecurityException("Unauthorized task.");
        }

        if (delay.isNegative() || delay.compareTo(Duration.ofDays(1)) > 0) {
            throw new IllegalArgumentException("Invalid scheduling delay.");
        }

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(
                () -> {
                    try {
                        authorizedTask.run();
                    } finally {
                        scheduler.shutdown();
                    }
                },
                delay.toSeconds(),
                TimeUnit.SECONDS
        );

        System.out.println("Authorized internal task scheduled.");
    }

    private static void cleanTemporaryFiles() {
        System.out.println("Running authorized temporary-file cleanup.");
    }

    private static void generateReport() {
        System.out.println("Running authorized report generation.");
    }
}