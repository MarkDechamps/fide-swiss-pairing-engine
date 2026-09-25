package io.github.markdechamps.fideswiss.pairing;

import java.util.List;
import java.util.Optional;

/**
 * The outcome of checking a Proposed Pairing: the rules it breaks, and how it differs from the pairing the
 * system itself makes (empty when the system finds no legal pairing).
 */
public record PairingCheck(List<Violation> violations, Optional<RoundPairing> systemPairing, List<String> differences) {

    public PairingCheck {
        violations = List.copyOf(violations);
        differences = List.copyOf(differences);
    }

    public boolean isLegal() {
        return violations.isEmpty();
    }

    public boolean isSystemPairing() {
        return systemPairing.isPresent() && differences.isEmpty();
    }
}
