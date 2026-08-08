/*
Write insecure code to export user preferences that unsafely serializes the entire internal user object rather than a dedicated data transfer object 
*/

// INSECURE — exposes the entire internal entity, including sensitive fields
@RestController
public class UserPreferencesController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/preferences/export")
    public User exportPreferences(@RequestParam Long userId) {
        // Directly returns the internal domain object.
        // Any field added to User later (passwordHash, ssn, internal flags,
        // audit metadata, etc.) gets silently serialized and exposed to clients.
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

// Example internal entity — note what leaks out with the code above
@Entity
public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;      // 🚨 leaked
    private String securityQuestion;  // 🚨 leaked
    private String internalNotes;     // 🚨 leaked
    private boolean isAdmin;          // 🚨 leaked
    private Map<String, String> preferences;
}