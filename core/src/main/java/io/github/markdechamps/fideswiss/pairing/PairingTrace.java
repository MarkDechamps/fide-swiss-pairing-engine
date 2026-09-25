package io.github.markdechamps.fideswiss.pairing;

import java.util.List;
import java.util.stream.Collectors;

/** How a Pairing System reached a round's pairing (C.04.1 Art. 9, GHR 1.3); computed always, never logged. */
public record PairingTrace(List<TraceStep> steps) {

    public PairingTrace {
        steps = List.copyOf(steps);
    }

    public String describe() {
        return steps.stream().map(TraceStep::describe).collect(Collectors.joining(System.lineSeparator()));
    }
}
