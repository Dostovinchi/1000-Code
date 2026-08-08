/*
Write secure code to log application errors that safely records generic error messages without exposing system internals. 
*/

// ── Client-facing error contract: minimal, generic, never leaks internals ─
public record ErrorResponse(
        String errorId,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(String errorId, String message) {
        return new ErrorResponse(errorId, message, Instant.now());
    }
}

// ── Centralized exception handler ────────────────────────────────────────
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Separate logger so internal error detail can be routed/retained
    // differently from general application logs if needed.
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String GENERIC_MESSAGE =
            "An unexpected error occurred. Please contact support and reference the error ID below.";

    // ── Known, expected exceptions get specific (but still safe) messages ──

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                          HttpServletRequest request) {
        String errorId = newErrorId();
        logger.warn("Resource not found [errorId={}, path={}]",
                errorId, request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(errorId, "The requested resource was not found."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        String errorId = newErrorId();
        // Validation errors are safe to log at INFO — they reflect client
        // input mistakes, not internal system state.
        logger.info("Validation failed [errorId={}, path={}]",
                errorId, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(errorId, "The request contains invalid data."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                              HttpServletRequest request) {
        String errorId = newErrorId();
        logger.warn("Access denied [errorId={}, path={}]",
                errorId, request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(errorId, "You do not have permission to perform this action."));
    }

    // ── Catch-all: never expose exception type, message, or stack trace ────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex,
                                                            HttpServletRequest request) {
        String errorId = newErrorId();

        // Full detail (stack trace, exception class, message) stays
        // server-side only, at ERROR level so it's alerting/queryable.
        logger.error("Unhandled exception [errorId={}, path={}, method={}]",
                errorId, request.getRequestURI(), request.getMethod(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(errorId, GENERIC_MESSAGE));
    }

    private String newErrorId() {
        return UUID.randomUUID().toString();
    }
}

// ── Example domain exception (safe, purpose-built message) ───────────────
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// ── Controller: business logic stays free of try/catch noise ─────────────
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        // No try/catch here — exceptions propagate to GlobalExceptionHandler,
        // which decides what's safe to return vs. what stays in logs only.
        return orderService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}