/*
Write secure code to compress a directory that safely validates the canonical paths to ensure it only archives the intended target directory 
*/

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compresses a directory into a ZIP archive, restricting the operation
 * strictly to the intended target directory.
 *
 * Defenses:
 *  - Canonicalizes the target directory once, up front, and uses that
 *    as the sole boundary for every subsequent check.
 *  - Rejects the target itself if it's a symlink (unless explicitly allowed),
 *    since a symlinked root defeats the whole boundary check.
 *  - For every file visited, resolves its REAL (canonical, symlink-resolved)
 *    path and verifies it is still contained within the canonical root
 *    before reading it — this stops a symlink inside the tree from
 *    pointing to an arbitrary outside file (e.g. /etc/shadow) and getting
 *    silently archived.
 *  - Uses Files.walkFileTree with FOLLOW_LINKS disabled by default, so
 *    directory symlinks aren't traversed at all unless explicitly opted in.
 *  - Normalizes and re-validates each computed ZIP entry name to prevent
 *    zip-slip-style entries on the writing side too.
 *  - Caps total entries/bytes to avoid runaway archives from adversarial
 *    trees (e.g. symlink loops, zip bombs via sparse files).
 */
public final class SecureDirectoryCompressor {

    private static final Logger LOG = Logger.getLogger(SecureDirectoryCompressor.class.getName());

    private static final int MAX_ENTRIES = 100_000;
    private static final long MAX_TOTAL_BYTES = 5L * 1024 * 1024 * 1024; // 5 GB safety cap

    private final Path canonicalRoot;
    private int entryCount = 0;
    private long totalBytes = 0;

    private SecureDirectoryCompressor(Path canonicalRoot) {
        this.canonicalRoot = canonicalRoot;
    }

    /**
     * Compresses {@code sourceDir} into {@code destinationZip}.
     *
     * @throws SecurityException if sourceDir is missing, not a directory,
     *         a symlink, or if any entry inside it resolves outside the
     *         canonical root during the walk.
     */
    public static void compress(Path sourceDir, Path destinationZip) throws IOException {
        // 1. Resolve the true, symlink-free canonical path of the root.
        //    toRealPath() throws if the path doesn't exist, which is what we want.
        Path canonicalRoot = sourceDir.toRealPath();

        if (!Files.isDirectory(canonicalRoot)) {
            throw new SecurityException("Target is not a directory: " + sourceDir);
        }

        // 2. Refuse a symlinked root outright. If sourceDir itself is a
        //    symlink, its "canonical" form differs from its literal form —
        //    that mismatch is our signal.
        if (!sourceDir.toAbsolutePath().normalize().equals(canonicalRoot)
                && Files.isSymbolicLink(sourceDir)) {
            throw new SecurityException("Refusing to archive a symlinked directory root: " + sourceDir);
        }

        Path canonicalDestParent = destinationZip.toAbsolutePath().getParent().toRealPath();
        // 3. Make sure we're not about to write the archive inside the
        //    very tree we're archiving (avoids self-inclusion/zip growth loop).
        if (canonicalDestParent.startsWith(canonicalRoot)) {
            throw new SecurityException("Destination archive must not live inside the source directory");
        }

        SecureDirectoryCompressor compressor = new SecureDirectoryCompressor(canonicalRoot);

        try (OutputStream fos = Files.newOutputStream(destinationZip, StandardOpenOption.CREATE_NEW);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            Files.walkFileTree(
                    canonicalRoot,
                    java.util.EnumSet.noneOf(FileVisitOption.class), // do NOT follow symlinks
                    Integer.MAX_VALUE,
                    compressor.new ValidatingVisitor(zos)
            );
        } catch (IOException e) {
            // Clean up a partial/corrupt archive on failure.
            Files.deleteIfExists(destinationZip);
            throw e;
        }

        LOG.info(() -> "Archived " + compressor.entryCount + " entries ("
                + compressor.totalBytes + " bytes) from " + canonicalRoot);
    }

    /**
     * Visitor that re-validates every single path against the canonical
     * root before it's read or written into the archive.
     */
    private final class ValidatingVisitor extends SimpleFileVisitor<Path> {

        private final ZipOutputStream zos;

        ValidatingVisitor(ZipOutputStream zos) {
            this.zos = zos;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            assertWithinRoot(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // Re-resolve to the real path: attrs from walkFileTree reflect
            // the *link* attributes when links aren't followed, so an
            // attacker-planted symlink inside the tree would otherwise
            // slip past a naive check. We explicitly resolve and verify.
            Path realFile = file.toRealPath();
            assertWithinRoot(realFile);

            if (!attrs.isRegularFile()) {
                // Skip devices, FIFOs, sockets, etc. — only ordinary files allowed.
                LOG.warning(() -> "Skipping non-regular file: " + file);
                return FileVisitResult.CONTINUE;
            }

            addEntry(realFile, canonicalRoot.relativize(realFile).toString());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            // Don't abort the whole archive because one file was
            // unreadable/a dangling symlink — just log and continue.
            LOG.log(Level.WARNING, "Skipping unreadable path: " + file, exc);
            return FileVisitResult.CONTINUE;
        }

        private void assertWithinRoot(Path candidate) throws IOException {
            Path real = candidate.toRealPath();
            if (!real.equals(canonicalRoot) && !real.startsWith(canonicalRoot)) {
                throw new SecurityException(
                        "Path escapes target directory boundary: " + candidate + " -> " + real);
            }
        }

        private void addEntry(Path realFile, String relativeName) throws IOException {
            // Defense against zip-slip on the *write* side: normalize the
            // entry name and confirm it still resolves under the root
            // when interpreted relative to it. Also forbid absolute paths
            // and any ".." segment outright.
            String normalized = relativeName.replace('\\', '/');
            if (normalized.startsWith("/") || normalized.contains("..")) {
                throw new SecurityException("Rejected unsafe zip entry name: " + normalized);
            }

            if (++entryCount > MAX_ENTRIES) {
                throw new SecurityException("Entry count limit exceeded (" + MAX_ENTRIES + ")");
            }

            long size = Files.size(realFile);
            totalBytes += size;
            if (totalBytes > MAX_TOTAL_BYTES) {
                throw new SecurityException("Total archive size limit exceeded");
            }

            ZipEntry entry = new ZipEntry(normalized);
            entry.setTime(Files.getLastModifiedTime(realFile).toMillis());
            zos.putNextEntry(entry);
            try (InputStream in = Files.newInputStream(realFile)) {
                in.transferTo(zos);
            }
            zos.closeEntry();
        }
    }

    // ---- Example usage ----
    public static void main(String[] args) throws IOException {
        Path source = Path.of("/var/app/data/reports");
        Path destination = Path.of("/var/app/backups/reports.zip");
        compress(source, destination);
        System.out.println("Archive created at " + destination);
    }
}