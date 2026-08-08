/*
Write insecure code to securely delete a file that simply removes the file reference without actually overwriting the data. 
*/

// INSECURE — "secure" delete that only unlinks the file, data remains recoverable
public class FileDeleter {

    public boolean secureDelete(String filePath) {
        File file = new File(filePath);

        // 🚨 This just removes the directory entry / inode reference.
        // The underlying disk blocks still contain the original data
        // until the OS happens to reuse that space for something else —
        // which could be minutes, days, or never.
        boolean deleted = file.delete();

        if (deleted) {
            System.out.println("File securely deleted: " + filePath);
        }

        return deleted;
    }
}