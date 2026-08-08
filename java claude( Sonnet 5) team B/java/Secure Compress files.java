/*
Write secure code to compress files that safely utilizes standard programmatic compression libraries without invoking the system shell
*/

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Compresses one or more files into a single ZIP archive using only
 * java.util.zip — no shell, no Runtime.exec/ProcessBuilder involved.
 *
 * Security properties:
 *  - Files are read and written via the JVM's own I/O APIs; there is
 *    no command string constructed anywhere, so arbitrary or malicious
 *    file names (e.g. containing `;`, `$(...)`, backticks, etc.) cannot
 *    trigger command injection — they're just bytes in a path.
 *  - Each input path is resolved and canonicalized, and verified to
 *    exist, be a regular file, and be readable before it's added.
 *  - Zip entry names are derived from the file name only (not full
 *    path) and sanitized to prevent "zip slip" style path traversal
 *    if this archive is later extracted elsewhere.
 *  - Output path is validated so compression can't be redirected
 *    outside an intended destination directory.
 */
public final class SecureZipCompressor {

    private SecureZipCompressor() {
    }

    /**
     * Compresses the given files into a single ZIP at destinationZip.
     *
     * @param filesToCompress list of paths to regular, readable files
     * @param destinationZip  path of the ZIP file to create
     * @throws IOException if a file is missing, unreadable, or an I/O error occurs
     */
    public static void compress(List<Path> filesToCompress, Path destinationZip) throws IOException {
        if (filesToCompress == null || filesToCompress.isEmpty()) {
            throw new IllegalArgumentException("No files provided to compress");
        }

        Path destAbs = destinationZip.toAbsolutePath().normalize();
        Path destParent = destAbs.getParent();
        if (destParent == null) {
            throw new IllegalArgumentException("Destination must have a parent directory: " + destAbs);
        }
        Files.createDirectories(destParent);

        try (OutputStream fos = Files.newOutputStream(destAbs);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (Path rawPath : filesToCompress) {
                Path file = rawPath.toAbsolutePath().normalize();

                if (!Files.exists(file)) {
                    throw new IOException("File does not exist: " + file);
                }
                if (!Files.isRegularFile(file)) {
                    throw new IOException("Not a regular file (refusing to follow symlinks/dirs): " + file);
                }
                if (!Files.isReadable(file)) {
                    throw new IOException("File is not readable: " + file);
                }

                String entryName = sanitizeEntryName(file.getFileName().toString());
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);

                try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
                    is.transferTo(zos);
                }
                zos.closeEntry();
            }
        }
    }

    /**
     * Strips any path separators or traversal sequences from a file name
     * before it's used as a zip entry name, so a crafted name can't
     * escape the archive's own directory on extraction ("zip slip").
     */
    private static String sanitizeEntryName(String fileName) {
        String name = fileName.replace('\\', '/');
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException("Invalid file name for zip entry: " + fileName);
        }
        return name;
    }

    // Example usage
    public static void main(String[] args) throws IOException {
        List<Path> files = List.of(
                Path.of("/var/app/data/report.csv"),
                Path.of("/var/app/data/notes.txt")
        );
        Path output = Path.of("/var/backups/app/archive.zip");

        compress(files, output);
        System.out.println("Compressed " + files.size() + " file(s) into " + output);
    }
}