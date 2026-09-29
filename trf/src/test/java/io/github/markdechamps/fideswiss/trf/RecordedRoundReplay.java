package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Interpretation;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.SwissPairingException;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Re-pairs every recorded round of a TRF and lists the rounds whose pairing differs from the file's. */
final class RecordedRoundReplay {

    private RecordedRoundReplay() {}

    static List<String> differences(String name, String trf) {
        return differences(name, TrfReader.read(trf));
    }

    /** The same, pairing under the given Swiss Rules Edition whatever the file declares. */
    static List<String> differences(String name, String trf, SwissRulesEdition edition) {
        var file = TrfReader.read(trf);
        return differences(name, file.with(file.settings().with(edition)));
    }

    /** The same, pairing under the given Interpretations whatever the file's system reads. */
    static List<String> differences(String name, String trf, Interpretation... interpretations) {
        var file = TrfReader.read(trf);
        var settings = file.settings();
        for (var interpretation : interpretations) {
            settings = settings.with(interpretation);
        }
        return differences(name, file.with(settings));
    }

    private static List<String> differences(String name, TrfTournament file) {
        var differences = new ArrayList<String>();
        for (var index = 0; index < file.recordedRounds().size(); index++) {
            var number = RoundNumber.of(index + 1);
            var recorded = file.recordedRounds().get(index);
            try {
                var pairing = file.tournamentBefore(number).pairNextRound();
                if (!boards(pairing).equals(boards(recorded)) || !bye(pairing).equals(bye(recorded))) {
                    differences.add(name + " round " + number + ": file " + describe(boards(recorded), bye(recorded))
                            + " | ours " + describe(boards(pairing), bye(pairing)));
                }
            } catch (SwissPairingException | IllegalStateException e) {
                differences.add(name + " round " + number + ": " + e);
            }
        }
        return differences;
    }

    private static Set<String> boards(RoundPairing pairing) {
        return pairing.boards().stream()
                .map(board -> board.white() + "-" + board.black())
                .collect(Collectors.toSet());
    }

    private static Set<String> boards(Round round) {
        return round.boards().stream()
                .map(board -> board.white() + "-" + board.black())
                .collect(Collectors.toSet());
    }

    private static String bye(RoundPairing pairing) {
        return pairing.pairingAllocatedBye().map(Object::toString).orElse("-");
    }

    private static String bye(Round round) {
        return round.byes().entrySet().stream()
                .filter(entry -> entry.getValue() == Bye.PAIRING_ALLOCATED)
                .map(entry -> entry.getKey().toString())
                .findFirst()
                .orElse("-");
    }

    private static String describe(Set<String> boards, String bye) {
        return boards.stream().sorted().collect(Collectors.joining(" ")) + " PAB " + bye;
    }
}
