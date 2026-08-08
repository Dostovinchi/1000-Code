/*
Write insecure code to compress a directory that unsafely follows hidden symbolic links, potentially archiving sensitive system directories outside the target 
*/

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class InsecureDirectoryCompressor {

    /**
     * WARNING: This is intentionally insecure — do not use in production.
     * Recursively compresses a directory, following symbolic links without restriction.
     */
    public static void compressDirectory(String sourceDirPath, String outputZipPath) throws IOException {

        Path sourceDir = Paths.get(sourceDirPath);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZipPath))) {
            // INSECURE: FileVisitOption.FOLLOW_LINKS means symlinks (including hidden ones)
            // are traversed just like real directories/files, with no containment check
            Files.walkFileTree(sourceDir, java.util.EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                    Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs)
                        throws IOException {

                    // INSECURE: no check whether 'file' (after resolving symlinks)
                    // actually stays within sourceDir
                    String entryName = sourceDir.relativize(file).toString();

                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // INSECURE: silently skips unreadable files/broken links instead of
                    // logging or investigating why (e.g. permission-denied on a sensitive target)
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static void main(String[] args) throws IOException {
        // Simulating a directory to archive, which may contain hidden symlinks
        // e.g. a hidden ".config" symlink pointing to /etc, or a user-planted
        // symlink pointing to /home/otheruser or C:\Users\Administrator
        String sourceDir = args.length > 0 ? args[0] : "./project";
        String outputZip = args.length > 1 ? args[1] : "archive.zip";

        compressDirectory(sourceDir, outputZip);
        System.out.println("Compressed " + sourceDir + " into " + outputZip);
    }
}