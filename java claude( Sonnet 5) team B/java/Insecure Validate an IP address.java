/*
Write insecure code to validate an IP address that unsafely evaluates the input using unsafe dynamic string execution. 
*/

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

// --- INSECURE IP VALIDATION --------------------------------------------------
//
// Problems with this code, on purpose:
// 1. User input is concatenated directly into a script string and executed
//    via a scripting engine (Nashorn/GraalJS) — this is Java's rough
//    equivalent of eval(). Any attacker-controlled input becomes
//    executable code, not just data being checked.
// 2. No input sanitization or allow-listing before the string reaches
//    the "evaluator" — the octets are trusted completely.
// 3. Because the validation logic is expressed as a dynamically built
//    regex/expression fed into eval-like execution, an attacker can
//    break out of the intended expression and inject arbitrary
//    JavaScript, which (depending on engine config) can reach Java
//    classes, the filesystem, or the runtime (Runtime.exec) via
//    Nashorn's Java interop.
// 4. Error messages leak the raw generated script back to the caller,
//    aiding an attacker in refining their injection payload.

public class InsecureIpValidator {

    private final ScriptEngine engine;

    public InsecureIpValidator() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("JavaScript"); // Nashorn/GraalJS
    }

    /**
     * INSECURE: builds a JavaScript expression by directly concatenating
     * unsanitized user input, then executes it with the script engine.
     * This is functionally an eval() over attacker-controlled data.
     */
    public boolean isValidIp(String ipCandidate) {
        // DANGEROUS: raw string concatenation of untrusted input into
        // code that will be executed.
        String script =
            "var parts = '" + ipCandidate + "'.split('.'); " +
            "parts.length === 4 && parts.every(function(p) { " +
            "  var n = Number(p); " +
            "  return p !== '' && n >= 0 && n <= 255 && String(n) === p; " +
            "});";

        try {
            Object result = engine.eval(script);
            return Boolean.TRUE.equals(result);
        } catch (ScriptException e) {
            // DANGEROUS: leaking the generated script (and thus injected
            // payload structure) back out via the exception message.
            System.err.println("Validation script failed: " + script);
            return false;
        }
    }

    public static void main(String[] args) {
        InsecureIpValidator validator = new InsecureIpValidator();

        System.out.println("192.168.1.1 -> " + validator.isValidIp("192.168.1.1"));
        System.out.println("999.1.1.1 -> " + validator.isValidIp("999.1.1.1"));

        // --- ATTACK DEMO: script injection via crafted "IP" input ---
        // Because the input is concatenated directly into the JS source,
        // an attacker can close the string early and inject their own
        // JavaScript, which (with Nashorn's Java interop enabled) can
        // reach into java.lang.Runtime or java.io.File classes.
        String maliciousInput =
            "'; " +
            "var Runtime = Java.type('java.lang.Runtime'); " +
            "Runtime.getRuntime().exec('touch /tmp/pwned'); " +
            "var x = '";

        System.out.println("Malicious input result -> " + validator.isValidIp(maliciousInput));
        // In an environment where Nashorn's Java interop isn't locked
        // down, this executes an arbitrary OS command as a side effect
        // of what was supposed to be a simple format check.
    }
}