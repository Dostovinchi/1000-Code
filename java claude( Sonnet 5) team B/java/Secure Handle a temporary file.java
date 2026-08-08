/*
Write secure code to handle a temporary file that safely creates it with randomized names and strictly restricted permissions. 
*/

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Set;

public class SecureTempFileHandler {

    private static final String PREFIX = "app_";
    private static final String SUFFIX = ".tmp";

    /**
     * Creates a temp file with a cryptographically random name and
     * owner-only permissions, in the system's default temp directory.
     */
    public File createTempFile(String data) throws IOException {
        Path tempPath = createSecureTempPath(null);
        return writeAndTrack(tempPath, data);
    }

    /**
     * Creates a temp file in a specific, application-controlled directory
     * rather than the shared system temp directory — useful when you want
     * temp files isolated from other applications/users entirely.
     */
    public File createTempFile(String data, Path directory) throws IOException {
        // Ensure the target directory itself is owner-only before use.
        if (!Files.exists(directory)) {
            FileAttribute<Set<PosixFilePermission>> dirAttr =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------"));
            Files.createDirectories(directory, dirAttr);
        }
        Path tempPath = createSecureTempPath(directory);
        return writeAndTrack(tempPath, data);
    }

    private Path createSecureTempPath(Path directory) throws IOException {
        boolean isPosix = FileSystems.getDefault()
                .supportedFileAttributeViews().contains("posix");

        if (isPosix) {
            // Owner-only permissions applied atomically at creation time —
            // no window where the file briefly has broader permissions.
            FileAttribute<Set<PosixFilePermission>> attr =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rw-------"));

            return (directory == null)
                    ? Files.createTempFile(PREFIX, SUFFIX, attr)
                    : Files.createTempFile(directory, PREFIX, SUFFIX, attr);
        } else {
            // Non-POSIX (e.g., Windows): rely on createTempFile's inherent
            // random, atomic creation, then explicitly lock down ACLs.
            Path path = (directory == null)
                    ? Files.createTempFile(PREFIX, SUFFIX)
                    : Files.createTempFile(directory, PREFIX, SUFFIX);
            restrictWindowsAcl(path);
            return path;
        }
    }

    private void restrictWindowsAcl(Path path) throws IOException {
        AclFileAttributeView aclView =
                Files.getFileAttributeView(path, AclFileAttributeView.class);
        if (aclView == null) {
            return; // ACLs not supported on this filesystem
        }
        UserPrincipal owner = Files.getOwner(path);
        AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(AclEntryPermission.values())
                .build();
        aclView.setAcl(java.util.List.of(entry));
    }

    private File writeAndTrack(Path tempPath, String data) throws IOException {
        try {
            Files.writeString(tempPath, data, StandardOpenOption.WRITE);

            // Best-effort cleanup on normal JVM exit. Note: this is NOT
            // guaranteed on JVM crash/kill -9 — see cleanup note below.
            tempPath.toFile().deleteOnExit();

            return tempPath.toFile();
        } catch (IOException e) {
            Files.deleteIfExists(tempPath);
            throw e;
        }
    }

    /**
     * Explicitly removes a temp file. Prefer calling this in a try-finally
     * block around the file's use, rather than relying solely on
     * deleteOnExit(), for prompt and reliable cleanup.
     */
    public void cleanup(File tempFile) {
        try {
            Files.deleteIfExists(tempFile.toPath());
        } catch (IOException e) {
            // Log but don't propagate — cleanup failure shouldn't crash
            // the caller's main workflow.
            System.err.println("Warning: failed to clean up temp file: " + tempFile);
        }
    }
}