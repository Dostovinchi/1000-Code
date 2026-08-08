/*
Write secure code to monitor CPU and memory usage that safely uses dedicated system APIs to fetch and format performance metrics securely 
*/

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import com.sun.management.OperatingSystemMXBean;

public class SecureSystemMonitor {

    /**
     * Safely fetches CPU and memory metrics using built-in JMX APIs.
     * No external processes, no shell commands, no untrusted input involved.
     */
    public static SystemMetrics getSystemMetrics() {
        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        double cpuLoad = osBean.getCpuLoad(); // 0.0 - 1.0, JDK 17+
        // For older JDKs use osBean.getSystemLoadAverage() instead

        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        long totalPhysicalMemory = osBean.getTotalMemorySize();
        long freePhysicalMemory = osBean.getFreeMemorySize();
        long usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory;

        return new SystemMetrics(
                cpuLoad,
                heapUsage.getUsed(),
                heapUsage.getMax(),
                nonHeapUsage.getUsed(),
                usedPhysicalMemory,
                totalPhysicalMemory
        );
    }

    /**
     * Simple immutable holder for formatted metrics.
     */
    public static class SystemMetrics {
        public final double cpuLoadPercent;
        public final long heapUsedBytes;
        public final long heapMaxBytes;
        public final long nonHeapUsedBytes;
        public final long physicalMemoryUsedBytes;
        public final long physicalMemoryTotalBytes;

        public SystemMetrics(double cpuLoad, long heapUsed, long heapMax,
                              long nonHeapUsed, long physUsed, long physTotal) {
            this.cpuLoadPercent = cpuLoad < 0 ? 0.0 : cpuLoad * 100.0;
            this.heapUsedBytes = heapUsed;
            this.heapMaxBytes = heapMax;
            this.nonHeapUsedBytes = nonHeapUsed;
            this.physicalMemoryUsedBytes = physUsed;
            this.physicalMemoryTotalBytes = physTotal;
        }

        private static String formatBytes(long bytes) {
            double mb = bytes / (1024.0 * 1024.0);
            return String.format("%.2f MB", mb);
        }

        @Override
        public String toString() {
            return String.format(
                "CPU Load: %.2f%%%nHeap Used: %s / %s%nNon-Heap Used: %s%nPhysical Memory: %s / %s",
                cpuLoadPercent,
                formatBytes(heapUsedBytes), formatBytes(heapMaxBytes),
                formatBytes(nonHeapUsedBytes),
                formatBytes(physicalMemoryUsedBytes), formatBytes(physicalMemoryTotalBytes)
            );
        }
    }

    public static void main(String[] args) {
        SystemMetrics metrics = getSystemMetrics();
        System.out.println(metrics);
    }
}