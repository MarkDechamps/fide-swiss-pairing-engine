package io.github.markdechamps.fideswiss.oracleit;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * One round as an Oracle pairs it: the boards as {@code white-black} in the file's own ids, and the participant
 * given the Pairing-Allocated Bye. The order of boards is not part of the pairing.
 */
public record OraclePairing(Set<String> boards, Optional<String> bye) {

    public OraclePairing {
        boards = Set.copyOf(boards);
    }

    /**
     * Reads the JaVaFo/bbpPairings reply (ADR 0005): a count line, then {@code white black} lines, with id 0 as the
     * black player for the PAB.
     */
    public static OraclePairing fromReply(String reply) {
        var lines =
                reply.lines().map(String::strip).filter(line -> !line.isEmpty()).toList();
        var boards = new TreeSet<String>();
        String bye = null;
        for (var line : lines.stream().skip(1).toList()) {
            var ids = Arrays.asList(line.split("\\s+"));
            if (ids.size() != 2) {
                throw new IllegalArgumentException("Not a pairing line: '" + line + "'");
            }
            if (ids.get(1).equals("0")) {
                bye = ids.get(0);
            } else {
                boards.add(ids.get(0) + "-" + ids.get(1));
            }
        }
        return new OraclePairing(boards, Optional.ofNullable(bye));
    }

    public String describe() {
        return boards.stream().sorted().collect(Collectors.joining(" ")) + " PAB " + bye.orElse("-");
    }
}
