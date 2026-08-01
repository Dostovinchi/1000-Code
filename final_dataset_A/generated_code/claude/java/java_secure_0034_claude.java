import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Profile Picture Upload Manager
 *
 * Allows users to upload, replace, and manage user profile picture files, storing
 * each one under a per-application storage directory using a filename
 * supplied by the user.
 *
 * This version follows secure coding practices to prevent Path Traversal
 * (CWE-22): the user-supplied filename is reduced to its base name, checked
 * against a strict allow-list of characters and extensions, and the final
 * resolved path is verified to remain inside the storage directory before
 * any file is written.
 */
public class java_secure_0034_claude {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    private final Path storageDir;
    private final List<String> uploadedFiles = new ArrayList<>();

    public java_secure_0034_claude(String storageDirPath) {
        this.storageDir = Paths.get(storageDirPath).toAbsolutePath().normalize();
        File dirFile = storageDir.toFile();
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    /**
     * Strips any directory components from the supplied name and rejects
     * empty names, hidden dot-files, and disallowed characters.
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
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new IllegalArgumentException("File name must include an allowed extension");
        }
        String extension = baseName.substring(dotIndex + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File extension not allowed: " + extension);
        }
        return baseName;
    }

    /**
     * SECURE: sanitizes the file name, resolves it against the storage
     * directory, and verifies the resulting canonical path is still inside
     * that directory before writing, which prevents path traversal.
     */
    public boolean uploadProfilePicture(String userSuppliedFileName, String content) {
        try {
            String safeName = sanitizeFileName(userSuppliedFileName);
            Path destination = storageDir.resolve(safeName).normalize();

            if (!destination.startsWith(storageDir)) {
                System.out.println("Rejected upload: resolved path escapes storage directory");
                return false;
            }

            try (FileWriter writer = new FileWriter(destination.toFile())) {
                writer.write(content);
            }
            uploadedFiles.add(safeName);
            System.out.println("Saved file to: " + destination);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected upload: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.out.println("Failed to save file: " + e.getMessage());
            return false;
        }
    }

    public List<String> listUploadedProfilePictures() {
        return uploadedFiles;
    }

    public static void main(String[] args) {
        String storagePath = System.getProperty("java.io.tmpdir") + File.separator + "profile_pictures";
        java_secure_0034_claude app = new java_secure_0034_claude(storagePath);

        // A normal, legitimate upload.
        app.uploadProfilePicture("jdoe_avatar.png", "binary-image-data-placeholder");

        // An attempted malicious upload using path traversal. The secure
        // implementation rejects it before any file is written.
        app.uploadProfilePicture("../../../../tmp/malicious_profile_pictures_dropped_file.txt", "attacker controlled content");

        System.out.println("Uploaded records: " + app.listUploadedProfilePictures());
    }
}
