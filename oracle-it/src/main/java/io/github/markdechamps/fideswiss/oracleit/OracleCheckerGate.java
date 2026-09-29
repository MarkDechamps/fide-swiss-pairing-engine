package io.github.markdechamps.fideswiss.oracleit;

import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * An Oracle's checker on our tournaments (Verification strategy, "our pairings, their checker"): each tournament our
 * generator played is written in the Oracle's dialect with all its rounds, and the Oracle's checker must accept it.
 * A rejection the register of Known Divergences recognises is reported apart.
 */
public final class OracleCheckerGate {

    /** A tournament the Oracle's checker did not accept, with the exact file it was given. */
    public record Rejection(String tournament, OracleVerdict.Rejected verdict, String input) {

        public String describe() {
            var lines = verdict.report().lines().limit(6).toList();
            return tournament + " (exit " + verdict.exitCode() + "): " + String.join(" / ", lines);
        }
    }

    public record Registered(String id, Rejection rejection) {}

    public record Report(
            String oracle, int tournaments, int accepted, List<Rejection> rejections, List<Registered> registered) {

        public String summary() {
            var counts = new TreeMap<String, Integer>();
            registered.forEach(entry -> counts.merge(entry.id(), 1, Integer::sum));
            return oracle + " checker on our tournaments: " + accepted + " of " + tournaments + " accepted, "
                    + rejections.size() + " rejected without a registered divergence, " + registered.size()
                    + " registered" + (counts.isEmpty() ? "" : " " + counts);
        }
    }

    private final OracleChecker checker;
    private final KnownDivergences register;

    public OracleCheckerGate(OracleChecker checker, KnownDivergences register) {
        this.checker = checker;
        this.register = register;
    }

    public Report compare(List<GeneratedTournament.Completed> tournaments) {
        var accepted = 0;
        var rejections = new ArrayList<Rejection>();
        var registered = new ArrayList<Registered>();
        for (var completed : tournaments) {
            var name = "seed-" + completed.seed();
            var input = checker.dialect().write(completed.tournament(), name);
            switch (checker.check(input)) {
                case OracleVerdict.Accepted ignored -> accepted++;
                case OracleVerdict.Rejected rejected -> {
                    var rejection = new Rejection(name, rejected, input);
                    var id = register.classify(checker.name(), input);
                    if (id.isPresent()) {
                        registered.add(new Registered(id.get(), rejection));
                    } else {
                        rejections.add(rejection);
                    }
                }
            }
        }
        return new Report(checker.name(), tournaments.size(), accepted, rejections, registered);
    }
}
