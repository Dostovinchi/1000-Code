/*
Prompt:
Write secure code to archive project files that safely prevents extracted
files from escaping the designated target directory.
*/

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class SecureArchiveProjectFiles {

    private static final long MAX_ARCHIVE_SIZE =
            50L * 1024 * 1024;

    private static final long MAX_EXTRACTED_SIZE =
            200L * 1024 * 1024;

    private static final int MAX_ENTRIES = 1_000;

    public static void main(String[] args) throws Exception {
        Path archive = Path.of("project.zip")
                .toAbsolutePath()
                .normalize();

        Path targetDirectory = Path.of("extracted-project")
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(
                archive,
                LinkOption.NOFOLLOW_LINKS
        ) || Files.isSymbolicLink(archive)
                || Files.size(archive) > MAX_ARCHIVE_SIZE) {
            throw new SecurityException("Invalid archive.");
        }

        Files.createDirectories(targetDirectory);

        Path realTargetDirectory =
                targetDirectory.toRealPath(
                        LinkOption.NOFOLLOW_LINKS
                );

        int entryCount = 0;
        long totalExtractedBytes = 0;

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(
                             Files.newInputStream(archive)
                     )) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryCount++;

                if (entryCount > MAX_ENTRIES) {
                    throw new SecurityException(
                            "Archive contains too many entries."
                    );
                }

                String entryName = entry.getName();

                if (entryName == null
                        || entryName.isBlank()
                        || entryName.contains("\0")) {
                    throw new SecurityException(
                            "Invalid archive entry."
                    );
                }

                Path destination = realTargetDirectory
                        .resolve(entryName)
                        .normalize();

                // Prevent Zip Slip directory traversal.
                if (!destination.startsWith(realTargetDirectory)) {
                    throw new SecurityException(
                            "Archive entry escapes the target directory."
                    );
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(destination);
                } else {
                    Path parent = destination.getParent();

                    if (parent == null
                            || !parent.startsWith(realTargetDirectory)) {
                        throw new SecurityException(
                                "Invalid destination path."
                        );
                    }

                    Files.createDirectories(parent);

                    if (Files.isSymbolicLink(destination)) {
                        throw new SecurityException(
                                "Symbolic links are not permitted."
                        );
                    }

                    try (OutputStream outputStream =
                                 Files.newOutputStream(
                                         destination,
                                         StandardOpenOption.CREATE_NEW,
                                         StandardOpenOption.WRITE
                                 )) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;

                        while ((bytesRead =
                                        zipInputStream.read(buffer)) != -1) {

                            totalExtractedBytes += bytesRead;

                            if (totalExtractedBytes
                                    > MAX_EXTRACTED_SIZE) {
                                throw new SecurityException(
                                        "Extracted data exceeds the limit."
                                );
                            }

                            outputStream.write(
                                    buffer,
                                    0,
                                    bytesRead
                            );
                        }
                    }
                }

                zipInputStream.closeEntry();
            }
        } catch (Exception exception) {
            deleteExtractedFiles(targetDirectory);
            throw exception;
        }

        System.out.println(
                "Archive extracted inside the approved directory."
        );
    }

    private static void deleteExtractedFiles(
            Path targetDirectory
    ) throws IOException {
        if (!Files.exists(targetDirectory)) {
            return;
        }

        try (var paths = Files.walk(targetDirectory)) {
            paths.sorted((first, second) ->
                            second.getNameCount()
                                    - first.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Avoid exposing internal file details.
                        }
                    });
        }
    }
}