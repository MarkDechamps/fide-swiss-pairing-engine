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

    /** The outcome of one run: how many rounds were compared, how many agreed, and each tournament's first difference. */
    public record Report(String oracle, int tournaments, int rounds, int agreements, List<Difference> differences) {

        public String summary() {
            return oracle + ": " + agreements + " of " + rounds + " rounds agree in " + tournaments + " tournaments, "
                    + differences.size() + " with a difference";
        }
    }

    private final PairingOracle oracle;
    private final SwissRulesEdition edition;

    public OracleGate(PairingOracle oracle, SwissRulesEdition edition) {
        this.oracle = oracle;
        this.edition = edition;
    }

    public Report compare(List<GeneratedTournament.Completed> tournaments) {
        var differences = new ArrayList<Difference>();
        var rounds = 0;
        var agreements = 0;
        for (var completed : tournaments) {
            var outcome = compare(completed);
            rounds += outcome.compared();
            agreements += outcome.agreed();
            outcome.difference().ifPresent(differences::add);
        }
        return new Report(oracle.name(), tournaments.size(), rounds, agreements, differences);
    }

    private record Outcome(int compared, int agreed, Optional<Difference> difference) {}

    private Outcome compare(GeneratedTournament.Completed completed) {
        var name = "seed-" + completed.seed();
        var file = read(completed.tournament());
        var compared = 0;
        for (var index = 0; index < file.recordedRounds().size(); index++) {
            var round = RoundNumber.of(index + 1);
            var before = file.tournamentBefore(round);
            var input = oracle.dialect().write(before, name);
            compared++;
            var ours = ours(before);
            var theirs = oracle.pair(input);
            if (!agree(ours, theirs)) {
                return new Outcome(
                        compared,
                        compared - 1,
                        Optional.of(new Difference(name, round, describe(ours), describe(theirs), input)));
            }
        }
        return new Outcome(compared, compared, Optional.empty());
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
