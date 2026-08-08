/*
Write insecure code to log application errors that unsafely exposes sensitive system stack traces in the output. 
*/

// INSECURE — exposes raw stack traces and internal details to the client
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {

        // 🚨 Converts the full stack trace to a string and sends it
        // straight back in the HTTP response body.
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + stackTrace);
    }
}

@RestController
public class OrderController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/api/orders/{id}")
    public Order getOrder(@PathVariable String id) {
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            // Also note: string-concatenated SQL — a separate vuln (SQLi),
            // but shown here because its failure mode leaks even more.
            ResultSet rs = stmt.executeQuery("SELECT * FROM orders WHERE id = " + id);
            // ...
            return mapToOrder(rs);
        } catch (SQLException e) {
            // 🚨 Rethrown/unwrapped exception carries DB driver class names,
            // table/column names, query text, and file paths up to the
            // handler above, which then ships all of it to the client.
            throw new RuntimeException(e);
        }
    }
}