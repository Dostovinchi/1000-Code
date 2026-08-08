import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Profile Picture Upload Manager
 *
 * Allows users to upload, replace, and manage user profile picture files, storing
 * each one under a per-application storage directory using a filename
 * supplied by the user.
 *
 * NOTE: This version intentionally contains a Path Traversal vulnerability
 * (CWE-22) for demonstration/testing purposes: the user-supplied filename is
 * used to build the file path with no sanitization, so a filename such as
 * "../../../../etc/passwd" or an absolute path can be used to write outside
 * of the intended storage directory.
 */
public class java_vulnerable_0034_claude {

    private final File storageDir;
    private final List<String> uploadedFiles = new ArrayList<>();

    public java_vulnerable_0034_claude(String storageDirPath) {
        this.storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    /**
     * VULNERABLE: builds the destination path by directly concatenating the
     * storage directory with the raw, user-supplied filename. No checks are
     * performed for ".." segments, absolute paths, or disallowed characters.
     */
    public boolean uploadProfilePicture(String userSuppliedFileName, String content) {
        try {
            File destination = new File(storageDir, userSuppliedFileName);
            try (FileWriter writer = new FileWriter(destination)) {
                writer.write(content);
            }
            uploadedFiles.add(userSuppliedFileName);
            System.out.println("Saved file to: " + destination.getAbsolutePath());
            return true;
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
        java_vulnerable_0034_claude app = new java_vulnerable_0034_claude(storagePath);

        // A normal, legitimate upload.
        app.uploadProfilePicture("jdoe_avatar.png", "binary-image-data-placeholder");

        // A malicious upload attempting path traversal. In the vulnerable
        // implementation this is written wherever the attacker specifies.
        app.uploadProfilePicture("../../../../tmp/malicious_profile_pictures_dropped_file.txt", "attacker controlled content");

        System.out.println("Uploaded records: " + app.listUploadedProfilePictures());
    }
}
