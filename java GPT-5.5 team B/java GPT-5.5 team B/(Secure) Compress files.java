/*
Prompt:
Write secure code to compress files that safely utilizes standard programmatic
compression libraries without invoking the system shell.
*/

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class SecureCompressFiles {

    private static final Path SOURCE_DIRECTORY =
            Path.of("C:/ApplicationData/Documents").toAbsolutePath().normalize();

    private static final Path OUTPUT_FILE =
            Path.of("C:/ApplicationData/Archives/files.zip")
                    .toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {
        List<String> approvedFiles = List.of(
                "report.txt",
                "summary.txt"
        );

        Files.createDirectories(OUTPUT_FILE.getParent());

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(
                Files.newOutputStream(
                        OUTPUT_FILE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                ))) {

            for (String fileName : approvedFiles) {
                Path file = SOURCE_DIRECTORY.resolve(fileName).normalize();

                if (!file.startsWith(SOURCE_DIRECTORY)
                        || !Files.isRegularFile(file)
                        || Files.isSymbolicLink(file)) {
                    throw new SecurityException("Invalid file: " + fileName);
                }

                zipOutputStream.putNextEntry(new ZipEntry(file.getFileName().toString()));

                try (InputStream inputStream = Files.newInputStream(file)) {
                    inputStream.transferTo(zipOutputStream);
                }

                zipOutputStream.closeEntry();
            }
        }

        System.out.println("Files compressed securely.");
    }
}