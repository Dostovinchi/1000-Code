/*
Write secure code to log audit events that safely sanitizes all data before writing it to the logs.
*/

// ── Sanitizer: single source of truth for making input log-safe ─────────
@Component
public final class LogSanitizer {

    private static final int MAX_FIELD_LENGTH = 256;
    // Strip CR, LF, tab, and all other control/non-printable characters
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\r\\n\\t\\x00-\\x1F\\x7F]");

    public String sanitize(String input) {
        if (input == null) {
            return "";
        }
        String stripped = CONTROL_CHARS.matcher(input).replaceAll("_");
        return stripped.length() > MAX_FIELD_LENGTH
                ? stripped.substring(0, MAX_FIELD_LENGTH) + "...[truncated]"
                : stripped;
    }
}

// ── Structured audit event (schema-defined, not free text) ──────────────
public final class AuditEvent {
    private final Instant timestamp;
    private final String eventType;
    private final String username;
    private final String sourceIp;
    private final String outcome;

    private AuditEvent(Builder b) {
        this.timestamp = Instant.now();
        this.eventType = b.eventType;
        this.username = b.username;
        this.sourceIp = b.sourceIp;
        this.outcome = b.outcome;
    }

    // Getters used by the JSON serializer
    public Instant getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }
    public String getUsername() { return username; }
    public String getSourceIp() { return sourceIp; }
    public String getOutcome() { return outcome; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String eventType;
        private String username;
        private String sourceIp;
        private String outcome;

        public Builder eventType(String v) { this.eventType = v; return this; }
        public Builder username(String v) { this.username = v; return this; }
        public Builder sourceIp(String v) { this.sourceIp = v; return this; }
        public Builder outcome(String v) { this.outcome = v; return this; }
        public AuditEvent build() { return new AuditEvent(this); }
    }
}

// ── Audit logger: sanitizes every field, then logs as structured JSON ───
@Component
public class AuditLogger {

    // Separate logger/appender for audit trail — typically routed to its
    // own file/index with stricter retention and access controls.
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final LogSanitizer sanitizer;
    private final ObjectMapper objectMapper;

    public AuditLogger(LogSanitizer sanitizer) {
        this.sanitizer = sanitizer;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void logLoginAttempt(String username, String sourceIp, boolean success) {
        AuditEvent event = AuditEvent.builder()
                .eventType("LOGIN_ATTEMPT")
                .username(sanitizer.sanitize(username))
                .sourceIp(sanitizer.sanitize(sourceIp))
                .outcome(success ? "SUCCESS" : "FAILURE")
                .build();

        writeStructured(event);
    }

    private void writeStructured(AuditEvent event) {
        try {
            // Serializing to JSON means fields are delimited by the schema,
            // not by raw whitespace/newlines — so even sanitizer bypasses
            // can't reshape the record structure.
            String json = objectMapper.writeValueAsString(event);
            auditLog.info(json);
        } catch (JsonProcessingException e) {
            auditLog.error("Failed to serialize audit event: {}", e.getMessage());
        }
    }
}

// ── Controller ─────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuditLogger auditLogger;

    public LoginController(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                         HttpServletRequest request) {
        boolean success = authenticate(username, request);

        auditLogger.logLoginAttempt(username, request.getRemoteAddr(), success);

        return success
                ? ResponseEntity.ok("Login successful")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
    }

    private boolean authenticate(String username, HttpServletRequest request) {
        // authentication logic here
        return true;
    }
}