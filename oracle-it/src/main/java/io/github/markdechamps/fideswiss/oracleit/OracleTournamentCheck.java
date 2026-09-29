package io.github.markdechamps.fideswiss.oracleit;

import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.trf.TrfReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Our checker on an Oracle's own tournaments (Verification strategy, "their pairings, our checker"): each file the
 * Oracle generated is read, and every recorded round must be legal and the system's own pairing, as the CLI's
 * {@code check} finds it, and the points the file declares must be the ones computed. A file the library cannot read
 * counts as one inconsistency. The first inconsistent round of a file ends the file.
 */
public final class OracleTournamentCheck {

    /** A generated tournament with the name it is reported under. */
    public record Generated(String name, String trf) {}

    /** One thing our checker found wrong; {@code round} is 0 for the file as a whole. */
    public record Inconsistency(String tournament, int round, String what, String trf) {

        public String describe() {
            return tournament + (round == 0 ? "" : " round " + round) + ": " + what;
        }
    }

    public record Registered(String id, Inconsistency inconsistency) {}

    public record Report(
            String oracle,
            int tournaments,
            int rounds,
            int consistent,
            List<Inconsistency> inconsistencies,
            List<Registered> registered) {

        public String summary() {
            var counts = new TreeMap<String, Integer>();
            registered.forEach(entry -> counts.merge(entry.id(), 1, Integer::sum));
            return oracle + " tournaments checked by us: " + consistent + " of " + rounds
                    + " rounds legal and consistent in "
                    + tournaments + " tournaments, " + inconsistencies.size() + " tournaments with an unregistered"
                    + " inconsistency, " + registered.size() + " registered" + (counts.isEmpty() ? "" : " " + counts);
        }
    }

    private final SwissRulesEdition edition;
    private final KnownDivergences register;

    public OracleTournamentCheck(SwissRulesEdition edition, KnownDivergences register) {
        this.edition = edition;
        this.register = register;
    }

    public Report check(String oracle, List<Generated> tournaments) {
        var rounds = 0;
        var consistent = 0;
        var failures = new ArrayList<Inconsistency>();
        var registered = new ArrayList<Registered>();
        for (var generated : tournaments) {
            var outcome = check(generated);
            rounds += outcome.rounds();
            consistent += outcome.consistent();
            for (var inconsistency : outcome.found()) {
                var id = register.classify(oracle, normalised(generated.trf()));
                if (id.isPresent()) {
                    registered.add(new Registered(id.get(), inconsistency));
                } else {
                    failures.add(inconsistency);
                }
            }
        }
        return new Report(oracle, tournaments.size(), rounds, consistent, failures, registered);
    }

    private record Outcome(int rounds, int consistent, List<Inconsistency> found) {}

    private Outcome check(Generated generated) {
        var found = new ArrayList<Inconsistency>();
        try {
            var read = TrfReader.read(generated.trf());
            var file = read.with(read.settings().with(edition));
            var recorded = file.recordedRounds();
            if (recorded.isEmpty()) {
                found.add(inconsistency(generated, 0, "the file records no round"));
                return new Outcome(0, 0, found);
            }
            var consistent = 0;
            for (var index = 0; index < recorded.size(); index++) {
                var round = RoundNumber.of(index + 1);
                var check = file.tournamentBefore(round).check(ProposedPairing.of(recorded.get(index)));
                if (!check.isLegal()) {
                    found.add(inconsistency(generated, round.value(), "ILLEGAL " + check.violations()));
                } else if (!check.isSystemPairing()) {
                    found.add(inconsistency(generated, round.value(), "DIFFERENT " + check.differences()));
                } else {
                    consistent++;
                    continue;
                }
                return new Outcome(recorded.size(), consistent, found);
            }
            pointsOf(file, generated).ifPresent(found::add);
            return new Outcome(recorded.size(), consistent, found);
        } catch (RuntimeException e) {
            found.add(inconsistency(generated, 0, "not readable: " + e.getMessage()));
            return new Outcome(0, 0, found);
        }
    }

    private static java.util.Optional<Inconsistency> pointsOf(
            io.github.markdechamps.fideswiss.trf.TrfTournament file, Generated generated) {
        var standings = file.tournament()
                .standingsAfter(RoundNumber.of(file.recordedRounds().size()));
        for (var entry : file.declaredPoints().entrySet()) {
            var computed = standings.standing(entry.getKey()).score().points();
            if (!computed.equals(entry.getValue())) {
                return java.util.Optional.of(inconsistency(
                        generated,
                        0,
                        entry.getKey() + " has " + entry.getValue() + " points in the file, " + computed
                                + " computed"));
            }
        }
        return java.util.Optional.empty();
    }

    private static Inconsistency inconsistency(Generated generated, int round, String what) {
        return new Inconsistency(generated.name(), round, what, generated.trf());
    }

    private static String normalised(String trf) {
        return trf.replace("\r\n", "\n").replace('\r', '\n');
    }
}
