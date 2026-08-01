import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice PDF Download Service
 *
 * Allows users to request and retrieve invoice PDF files by name from a
 * per-application storage directory, and to view a listing of what is
 * currently available.
 *
 * This version follows secure coding practices to prevent Path Traversal
 * (CWE-22): the user-supplied file name is reduced to its base name,
 * validated against a strict allow-list of characters, and the resolved
 * path is verified to remain inside the storage directory before any file
 * is read.
 */
public class java_secure_0037_claude {

    private final Path storageDir;

    public java_secure_0037_claude(String storageDirPath) {
        this.storageDir = Paths.get(storageDirPath).toAbsolutePath().normalize();
        File dirFile = storageDir.toFile();
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    private void seedSampleFile(String name, String content) {
        try (FileWriter writer = new FileWriter(storageDir.resolve(name).toFile())) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("Could not seed sample file: " + e.getMessage());
        }
    }

    public List<String> listInvoices() {
        List<String> names = new ArrayList<>();
        File[] files = storageDir.toFile().listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    names.add(f.getName());
                }
            }
        }
        return names;
    }

    /**
     * Strips any directory components from the supplied name and rejects
     * empty names, "." and ".." segments, and disallowed characters.
     */
    private String sanitizeFileName(String userSuppliedFileName) {
        if (userSuppliedFileName == null) {
            throw new IllegalArgumentException("File name must not be null");
        }
        String baseName = Paths.get(userSuppliedFileName).getFileName().toString();
        if (baseName.isEmpty() || baseName.equals(".") || baseName.equals("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }
        if (!baseName.matches("^[A-Za-z0-9._-]+$")) {
            throw new IllegalArgumentException("File name contains disallowed characters");
        }
        return baseName;
    }

    /**
     * SECURE: sanitizes the file name, resolves it against the storage
     * directory, and verifies the resulting canonical path is still inside
     * that directory before reading, which prevents path traversal and
     * unauthorized access to files outside the storage area.
     */
    public String fetchInvoice(String userSuppliedFileName) {
        try {
            String safeName = sanitizeFileName(userSuppliedFileName);
            Path target = storageDir.resolve(safeName).normalize();

            if (!target.startsWith(storageDir)) {
                return "Error: requested file is outside the allowed storage area";
            }
            if (!Files.exists(target) || !Files.isRegularFile(target)) {
                return "Error: file not found";
            }

            byte[] data = Files.readAllBytes(target);
            return new String(data);
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String storagePath = System.getProperty("java.io.tmpdir") + File.separator + "invoices";
        java_secure_0037_claude app = new java_secure_0037_claude(storagePath);
        app.seedSampleFile("invoice_1001.pdf", "%PDF-1.4 sample invoice content");

        System.out.println("Available files: " + app.listInvoices());

        // A normal, legitimate request.
        System.out.println("---- Requested file content ----");
        System.out.println(app.fetchInvoice("invoice_1001.pdf"));

        // An attempted malicious request using path traversal. The secure
        // implementation rejects it before any file is read.
        String maliciousRequest = "../../../../etc/passwd";
        System.out.println("---- Malicious request result (rejected) ----");
        System.out.println(app.fetchInvoice(maliciousRequest));
    }
}
