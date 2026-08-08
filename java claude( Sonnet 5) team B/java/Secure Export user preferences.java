/*
Write secure code to export user preferences that safely maps the internal data to a specific, restricted object representing only public preferences before serialization
*/

// ── Internal domain entity (never exposed directly) ─────────────────────
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String securityQuestion;
    private String internalNotes;
    private boolean isAdmin;

    @ElementCollection
    private Map<String, String> preferences; // e.g. theme, language, notifications

    // getters/setters omitted for brevity
}

// ── Public-facing DTO: explicit allowlist of exportable fields ──────────
public final class UserPreferencesExportDTO {

    private final String username;
    private final String theme;
    private final String language;
    private final boolean emailNotificationsEnabled;

    public UserPreferencesExportDTO(String username, String theme,
                                     String language, boolean emailNotificationsEnabled) {
        this.username = username;
        this.theme = theme;
        this.language = language;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public String getUsername() { return username; }
    public String getTheme() { return theme; }
    public String getLanguage() { return language; }
    public boolean isEmailNotificationsEnabled() { return emailNotificationsEnabled; }
}

// ── Mapper: the only place internal fields are read ──────────────────────
@Component
public class UserPreferencesMapper {

    private static final String DEFAULT_THEME = "light";
    private static final String DEFAULT_LANGUAGE = "en";

    public UserPreferencesExportDTO toExportDTO(User user) {
        Map<String, String> prefs = user.getPreferences() != null
                ? user.getPreferences()
                : Collections.emptyMap();

        String theme = prefs.getOrDefault("theme", DEFAULT_THEME);
        String language = prefs.getOrDefault("language", DEFAULT_LANGUAGE);
        boolean emailNotifications = Boolean.parseBoolean(
                prefs.getOrDefault("emailNotificationsEnabled", "true"));

        // Only these four fields ever leave the system.
        // Adding a new field to User does NOT expose it here —
        // it must be deliberately added to this mapper.
        return new UserPreferencesExportDTO(
                user.getUsername(),
                theme,
                language,
                emailNotifications
        );
    }
}

// ── Controller ─────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/preferences")
public class UserPreferencesController {

    private final UserRepository userRepository;
    private final UserPreferencesMapper mapper;

    public UserPreferencesController(UserRepository userRepository,
                                      UserPreferencesMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @GetMapping("/export")
    public ResponseEntity<UserPreferencesExportDTO> exportPreferences(
            @AuthenticationPrincipal UserDetails principal) {

        // Use the authenticated principal's ID, never a caller-supplied one,
        // to prevent IDOR (users exporting other users' preferences).
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(mapper.toExportDTO(user));
    }
}