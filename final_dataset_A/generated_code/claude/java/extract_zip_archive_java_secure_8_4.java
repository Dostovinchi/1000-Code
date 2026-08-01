import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Task 8.4 (SECURE): Extract a ZIP archive.
 *
 * Secure practices:
 *  - Each entry's canonical destination path is validated to remain inside
 *    the target directory before writing (prevents "Zip Slip" / CWE-22).
 *  - Entries with absolute paths or ".." traversal segments are rejected
 *    outright.
 *  - Invalid or corrupted archives are handled gracefully.
 */
public class extract_zip_archive_java_secure_8_4 {

    static File safeResolve(File destDir, String entryName) throws IOException {
        if (entryName.startsWith("/") || entryName.startsWith("\\") || entryName.contains("..")) {
            throw new SecurityException("Rejected unsafe entry: " + entryName);
        }
        File destDirCanonical = destDir.getCanonicalFile();
        File target = new File(destDir, entryName).getCanonicalFile();
        String destPath = destDirCanonical.getPath() + File.separator;
        if (!(target.getPath().equals(destDirCanonical.getPath()) || target.getPath().startsWith(destPath))) {
            throw new SecurityException("Unsafe path traversal detected in entry: " + entryName);
        }
        return target;
    }

    static void extractArchive(String zipPath, String destDir) {
        File destDirFile = new File(destDir);
        destDirFile.mkdirs();

        try (ZipFile zip = new ZipFile(zipPath)) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File outFile = safeResolve(destDirFile, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                outFile.getParentFile().mkdirs();
                try (InputStream in = zip.getInputStream(entry);
                     FileOutputStream out = new FileOutputStream(outFile)) {
                    in.transferTo(out);
                }
            }
            System.out.println("Safely extracted " + zipPath + " into " + destDir);
        } catch (ZipException e) {
            System.out.println("Error: '" + zipPath + "' is not a valid ZIP archive.");
        } catch (SecurityException e) {
            System.out.println("Security error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error extracting archive: " + e.getMessage());
        }
    }

    static void createSampleZip(String zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            zos.putNextEntry(new ZipEntry("hello.txt"));
            zos.write("Hello from inside the archive!".getBytes());
            zos.closeEntry();
        }
    }

    public static void main(String[] args) throws IOException {
        String zipPath = "sample.zip";
        String destDir = "extracted_secure";
        if (!new File(zipPath).exists()) {
            createSampleZip(zipPath);
        }
        extractArchive(zipPath, destDir);
    }
}
