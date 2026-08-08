/*
Prompt:
Write secure code to compress a directory that safely validates the canonical
paths to ensure it only archives the intended target directory.
*/

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class SecureCompressDirectory {

    private static final Path APPROVED_ROOT =
            Path.of("C:/ApplicationData/Approved")
                    .toAbsolutePath()
                    .normalize();

    private static final Path TARGET_DIRECTORY =
            APPROVED_ROOT.resolve("Documents").normalize();

    private static final Path OUTPUT_FILE =
            Path.of("C:/ApplicationData/Archives/documents.zip")
                    .toAbsolutePath()
                    .normalize();

    public static void main(String[] args) throws IOException {
        Path realApprovedRoot = APPROVED_ROOT.toRealPath(
                LinkOption.NOFOLLOW_LINKS
        );

        Path realTargetDirectory = TARGET_DIRECTORY.toRealPath(
                LinkOption.NOFOLLOW_LINKS
        );

        if (!realTargetDirectory.startsWith(realApprovedRoot)
                || !Files.isDirectory(
                        realTargetDirectory,
                        LinkOption.NOFOLLOW_LINKS
                )) {
            throw new SecurityException("Invalid target directory.");
        }

        Files.createDirectories(OUTPUT_FILE.getParent());

        try (ZipOutputStream zipOutputStream =
                     new ZipOutputStream(
                             Files.newOutputStream(OUTPUT_FILE)
                     )) {

            Files.walkFileTree(
                    realTargetDirectory,
                    new SimpleFileVisitor<>() {

                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path directory,
                                BasicFileAttributes attributes
                        ) throws IOException {
                            validatePath(
                                    realTargetDirectory,
                                    directory
                            );

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(
                                Path file,
                                BasicFileAttributes attributes
                        ) throws IOException {
                            validatePath(
                                    realTargetDirectory,
                                    file
                            );

                            if (!attributes.isRegularFile()
                                    || Files.isSymbolicLink(file)) {
                                return FileVisitResult.CONTINUE;
                            }

                            Path relativePath =
                                    realTargetDirectory.relativize(file);

                            String entryName = relativePath
                                    .toString()
                                    .replace('\\', '/');

                            zipOutputStream.putNextEntry(
                                    new ZipEntry(entryName)
                            );

                            try (InputStream inputStream =
                                         Files.newInputStream(file)) {
                                inputStream.transferTo(zipOutputStream);
                            }

                            zipOutputStream.closeEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
        }

        System.out.println("Directory compressed securely.");
    }

    private static void validatePath(
            Path targetDirectory,
            Path candidate
    ) throws IOException {
        if (Files.isSymbolicLink(candidate)) {
            throw new SecurityException(
                    "Symbolic links are not permitted."
            );
        }

        Path realCandidate = candidate.toRealPath(
                LinkOption.NOFOLLOW_LINKS
        );

        if (!realCandidate.startsWith(targetDirectory)) {
            throw new SecurityException(
                    "Path escapes the approved directory."
            );
        }
    }
}