# Write secure code to validate password strength that safely enforces complex rules including various character types and numbers locally. 

import re
import unicodedata
from dataclasses import dataclass, field


@dataclass
class PasswordPolicy:
    min_length: int = 12
    max_length: int = 128          # prevents DoS via extremely long inputs
    require_lower: bool = True
    require_upper: bool = True
    require_digit: bool = True
    require_symbol: bool = True
    min_unique_chars: int = 6
    forbidden_substrings: list = field(default_factory=list)  # e.g. username, app name


@dataclass
class ValidationResult:
    is_valid: bool
    errors: list = field(default_factory=list)
    score: int = 0  # rough strength score out of 100


_SYMBOL_RE = re.compile(r"[!\"#$%&'()*+,\-./:;<=>?@\[\\\]^_`{|}~]")

# A small denylist of extremely common passwords. In production, check
# against a much larger local list (e.g. a copy of the "10k most common
# passwords" file) rather than a hardcoded few.
_COMMON_PASSWORDS = {
    "password", "123456", "123456789", "qwerty", "letmein",
    "admin", "welcome", "iloveyou", "monkey", "dragon",
}


def validate_password(password: str, policy: PasswordPolicy = PasswordPolicy()) -> ValidationResult:
    errors = []

    if not isinstance(password, str):
        return ValidationResult(False, ["Password must be a string."])

    # Normalize so visually-equivalent unicode forms are treated the same
    normalized = unicodedata.normalize("NFKC", password)

    # Work in code points, not bytes, so multi-byte chars count correctly
    length = len(normalized)

    if length < policy.min_length:
        errors.append(f"Password must be at least {policy.min_length} characters long.")
    if length > policy.max_length:
        errors.append(f"Password must be no more than {policy.max_length} characters long.")

    has_lower = any(c.islower() for c in normalized)
    has_upper = any(c.isupper() for c in normalized)
    has_digit = any(c.isdigit() for c in normalized)
    has_symbol = bool(_SYMBOL_RE.search(normalized))

    if policy.require_lower and not has_lower:
        errors.append("Password must include at least one lowercase letter.")
    if policy.require_upper and not has_upper:
        errors.append("Password must include at least one uppercase letter.")
    if policy.require_digit and not has_digit:
        errors.append("Password must include at least one digit.")
    if policy.require_symbol and not has_symbol:
        errors.append("Password must include at least one symbol.")

    unique_chars = len(set(normalized))
    if unique_chars < policy.min_unique_chars:
        errors.append(f"Password must contain at least {policy.min_unique_chars} unique characters.")

    if _has_long_run(normalized, run_len=4):
        errors.append("Password must not contain a character repeated 4+ times in a row.")

    if _has_sequential_run(normalized, run_len=4):
        errors.append("Password must not contain a simple sequential pattern (e.g. 'abcd', '1234').")

    lowered = normalized.lower()
    if lowered in _COMMON_PASSWORDS:
        errors.append("Password is too common; choose something less predictable.")

    for forbidden in policy.forbidden_substrings:
        if forbidden and forbidden.lower() in lowered:
            errors.append("Password must not contain your username or other personal info.")
            break

    score = _estimate_strength(normalized, has_lower, has_upper, has_digit, has_symbol, unique_chars)

    return ValidationResult(is_valid=len(errors) == 0, errors=errors, score=score)


def _has_long_run(s: str, run_len: int) -> bool:
    count = 1
    for i in range(1, len(s)):
        count = count + 1 if s[i] == s[i - 1] else 1
        if count >= run_len:
            return True
    return False


def _has_sequential_run(s: str, run_len: int) -> bool:
    """Detects ascending/descending sequences like 'abcd' or '4321'."""
    lowered = s.lower()
    count_up = count_down = 1
    for i in range(1, len(lowered)):
        prev, curr = ord(lowered[i - 1]), ord(lowered[i])
        count_up = count_up + 1 if curr - prev == 1 else 1
        count_down = count_down + 1 if prev - curr == 1 else 1
        if max(count_up, count_down) >= run_len:
            return True
    return False


def _estimate_strength(s, has_lower, has_upper, has_digit, has_symbol, unique_chars) -> int:
    """Rough 0-100 heuristic score — not a substitute for real entropy estimation
    (consider the `zxcvbn` library for that in production)."""
    pool = sum([has_lower * 26, has_upper * 26, has_digit * 10, has_symbol * 32])
    pool = max(pool, 1)
    bits = len(s) * (pool.bit_length() - 1) if pool > 1 else 0
    variety_bonus = sum([has_lower, has_upper, has_digit, has_symbol]) * 5
    uniqueness_bonus = min(unique_chars, 15)
    return min(100, bits // 2 + variety_bonus + uniqueness_bonus)


if __name__ == "__main__":
    import getpass

    pw = getpass.getpass("Enter password to check: ")
    result = validate_password(pw, PasswordPolicy(forbidden_substrings=["admin"]))
    if result.is_valid:
        print(f"✅ Password accepted. Strength score: {result.score}/100")
    else:
        print("❌ Password rejected:")
        for err in result.errors:
            print(f"  - {err}")