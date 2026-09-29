package io.github.markdechamps.fideswiss.oracleit;

import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.SwissPairingException;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.trf.TrfReader;
import io.github.markdechamps.fideswiss.trf.TrfTournament;
import io.github.markdechamps.fideswiss.trf.TrfWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Our pairings against an Oracle's (Verification strategy, "our pairings, their re-pairing"). For every round of
 * every generated tournament the Oracle pairs the round from the rounds before it, and the answer must equal the
 * library's, board for board and PAB for PAB. The first differing round of a tournament is reported and the rest of
 * that tournament is not compared.
 */
public final class OracleGate {

    /** One round the Oracle and the library paired differently, with the exact input the Oracle was given. */
    public record Difference(String tournament, RoundNumber round, String ours, String oracle, String input) {

        public String describe() {
            return tournament + " round " + round + ": ours " + ours + " | " + oracle;
        }
    }

    /** A difference the register of Known Divergences recognises, under its {@code id} (KD-n). */
    public record Registered(String id, Difference difference) {}

    /**
     * The outcome of one run: how many rounds were compared and agreed, the rounds that differ without being
     * registered (each tournament's first; these fail the gate) and the registered ones (all of them).
     */
    public record Report(
            String oracle,
            int tournaments,
            int rounds,
            int agreements,
            List<Difference> differences,
            List<Registered> registered) {

        public String summary() {
            return oracle + ": " + agreements + " of " + rounds + " rounds agree in " + tournaments + " tournaments, "
                    + differences.size() + " with an unregistered difference, " + registered.size()
                    + " registered rounds" + registeredCounts();
        }

        private String registeredCounts() {
            var counts = new java.util.TreeMap<String, Integer>();
            registered.forEach(entry -> counts.merge(entry.id(), 1, Integer::sum));
            return counts.isEmpty() ? "" : " " + counts;
        }
    }

    private final PairingOracle oracle;
    private final SwissRulesEdition edition;
    private final KnownDivergences register;

    public OracleGate(PairingOracle oracle, SwissRulesEdition edition) {
        this(oracle, edition, KnownDivergences.register());
    }

    public OracleGate(PairingOracle oracle, SwissRulesEdition edition, KnownDivergences register) {
        this.oracle = oracle;
        this.edition = edition;
        this.register = register;
    }

    public Report compare(List<GeneratedTournament.Completed> tournaments) {
        var differences = new ArrayList<Difference>();
        var registered = new ArrayList<Registered>();
        var rounds = 0;
        var agreements = 0;
        for (var completed : tournaments) {
            var outcome = compare(completed);
            rounds += outcome.compared();
            agreements += outcome.agreed();
            differences.addAll(outcome.unregistered());
            registered.addAll(outcome.registered());
        }
        return new Report(oracle.name(), tournaments.size(), rounds, agreements, differences, registered);
    }

    private record Outcome(int compared, int agreed, List<Difference> unregistered, List<Registered> registered) {}

    /**
     * Each round is paired by the Oracle from our history, so a registered difference does not spoil the rounds
     * after it; the first unregistered one ends the tournament.
     */
    private Outcome compare(GeneratedTournament.Completed completed) {
        var name = "seed-" + completed.seed();
        var file = read(completed.tournament());
        var registered = new ArrayList<Registered>();
        var compared = 0;
        var agreed = 0;
        for (var index = 0; index < file.recordedRounds().size(); index++) {
            var round = RoundNumber.of(index + 1);
            var before = file.tournamentBefore(round);
            var input = oracle.dialect().write(before, name);
            compared++;
            var ours = ours(before);
            var theirs = oracle.pair(input);
            if (agree(ours, theirs)) {
                agreed++;
                continue;
            }
            var difference = new Difference(name, round, describe(ours), describe(theirs), input);
            var id = register.classify(oracle.name(), input);
            if (id.isEmpty()) {
                return new Outcome(compared, agreed, List.of(difference), registered);
            }
            registered.add(new Registered(id.get(), difference));
        }
        return new Outcome(compared, agreed, List.of(), registered);
    }

    private TrfTournament read(Tournament tournament) {
        var file = TrfReader.read(TrfWriter.write(tournament, TrfWriter.Options.named("oracle gate")));
        return file.with(file.settings().with(edition));
    }

    private static Optional<OraclePairing> ours(Tournament before) {
        try {
            return Optional.of(of(before.pairNextRound()));
        } catch (SwissPairingException e) {
            return Optional.empty();
        }
    }

    private static OraclePairing of(RoundPairing pairing) {
        var boards = new TreeSet<String>();
        pairing.boards().forEach(board -> boards.add(board.white() + "-" + board.black()));
        return new OraclePairing(boards, pairing.pairingAllocatedBye().map(Object::toString));
    }

    /** Both refusing agree: no legal pairing exists, whatever either program says about why. */
    private static boolean agree(Optional<OraclePairing> ours, OracleAnswer theirs) {
        return switch (theirs) {
            case OracleAnswer.Paired paired ->
                ours.map(paired.pairing()::equals).orElse(false);
            case OracleAnswer.Refused refused -> ours.isEmpty();
        };
    }

    private static String describe(Optional<OraclePairing> ours) {
        return ours.map(OraclePairing::describe).orElse("no legal pairing");
    }

    private static String describe(OracleAnswer answer) {
        return switch (answer) {
            case OracleAnswer.Paired paired -> "oracle " + paired.pairing().describe();
            case OracleAnswer.Refused refused ->
                "oracle refused (exit " + refused.exitCode() + ") " + refused.message();
        };
    }
}
