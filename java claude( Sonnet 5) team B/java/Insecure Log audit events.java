/*
Write insecure code to log audit events that unsafely incorporates raw user input directly into the log file. 
*/

// INSECURE — raw user input written directly into log entries
@RestController
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    @PostMapping("/api/audit/login")
    public ResponseEntity<String> logLoginAttempt(@RequestParam String username,
                                                    HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        // 🚨 Raw, unsanitized input concatenated straight into the log line.
        logger.info("Login attempt for user: " + username + " from IP: " + ip);

        return ResponseEntity.ok("Logged");
    }
}