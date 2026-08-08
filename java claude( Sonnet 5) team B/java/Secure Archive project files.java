/*
Write secure code to archive project files that safely prevents extracted files from escaping the designated target directory
*/

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class SecureArchiveExtractor {

    // Defense-in-depth limits against zip bombs / resource exhaustion
    private static final long MAX_ENTRY_SIZE = 100L * 1024 * 1024;   // 100 MB per file
    private static final long MAX_TOTAL_SIZE = 1L * 1024 * 1024 * 1024; // 1 GB total
    private static final int MAX_ENTRIES = 10_000;

    public static void extractArchive(File zipFile, File targetDir) throws IOException {
        // Resolve the target directory to its canonical, absolute form up front
        Path targetDirNormalized = targetDir.toPath().toAbsolutePath().normalize();
        Files.createDirectories(targetDirNormalized);

        long totalSizeSoFar = 0;
        int entryCount = 0;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                entryCount++;
                if (entryCount > MAX_ENTRIES) {
                    throw new IOException("Archive contains too many entries — refusing to extract");
                }

                // Reject absolute paths and Windows drive-letter paths outright
                String entryName = entry.getName();
                if (entryName.isEmpty() || entryName.startsWith("/") || entryName.startsWith("\\")
                        || entryName.matches("^[A-Za-z]:.*")) {
                    throw new IOException("Rejected entry with unsafe path: " + entryName);
                }

                // Build the candidate output path and normalize it (resolves ".." segments)
                Path candidate = targetDirNormalized.resolve(entryName).normalize();

                // THE KEY CHECK: the resolved path must still live inside targetDir.
                // This catches "../../../etc/passwd" style traversal regardless of
                // how many ".." segments or what encoding tricks are used, because
                // normalize() collapses them before we compare.
                if (!candidate.startsWith(targetDirNormalized)) {
                    throw new IOException("Zip entry escapes target directory: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(candidate);
                    continue;
                }

                // Reject symlink entries — Java's ZipEntry doesn't expose symlink
                // status directly via ZipInputStream, but if the platform later
                // creates one, re-verify the real path resolves inside targetDir
                Files.createDirectories(candidate.getParent());

                long entrySize = 0;
                try (OutputStream fos = Files.newOutputStream(candidate,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        entrySize += len;
                        totalSizeSoFar += len;

                        if (entrySize > MAX_ENTRY_SIZE) {
                            throw new IOException("Entry exceeds max allowed size: " + entryName);
                        }
                        if (totalSizeSoFar > MAX_TOTAL_SIZE) {
                            throw new IOException("Archive exceeds max total extracted size");
                        }

                        fos.write(buffer, 0, len);
                    }
                }

                // Re-verify after writing: if the OS resolved any symlink components
                // in the parent path, confirm the real file still lives inside targetDir
                Path realPath = candidate.toRealPath();
                if (!realPath.startsWith(targetDirNormalized.toRealPath())) {
                    Files.delete(candidate);
                    throw new IOException("Extracted file resolved outside target directory: " + entryName);
                }
            }
        }
    }
}