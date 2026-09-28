package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;

/** One step of a {@link PairingTrace}. */
public sealed interface TraceStep {

    String describe();

    /** Who got the Pairing-Allocated Bye, and the article that decided it. */
    record ByeDecision(ParticipantId participant, String article) implements TraceStep {
        @Override
        public String describe() {
            return "PAB: " + participant + " [" + article + "]";
        }
    }

    /**
     * One bracket: its residents and moved-down participants, the pairs and downfloaters of the chosen candidate,
     * and how that candidate fails each criterion ({@code criteria}, only the failing ones).
     */
    record BracketStep(
            String label,
            List<ParticipantId> residents,
            List<ParticipantId> movedDown,
            List<List<ParticipantId>> pairs,
            List<ParticipantId> downfloaters,
            String criteria)
            implements TraceStep {

        public BracketStep {
            residents = List.copyOf(residents);
            movedDown = List.copyOf(movedDown);
            pairs = pairs.stream().map(List::copyOf).toList();
            downfloaters = List.copyOf(downfloaters);
        }

        @Override
        public String describe() {
            var pairsText =
                    pairs.stream().map(pair -> pair.get(0) + "-" + pair.get(1)).toList();
            return "bracket " + label + ": residents " + residents + ", moved down " + movedDown + "; pairs "
                    + pairsText + ", downfloaters " + downfloaters + (criteria.isEmpty() ? "" : "; fails " + criteria);
        }
    }

    /**
     * One bracket of the Top-Scoregroup Procedure (Swiss Team, Double-Swiss): the top-scoregroup's residents, the
     * upfloaters chosen for it, its pairs (top member first) and how they fail the bracket criteria.
     */
    record TopScoregroupBracket(
            String label,
            List<ParticipantId> residents,
            List<ParticipantId> upfloaters,
            List<List<ParticipantId>> pairs,
            String criteria)
            implements TraceStep {

        public TopScoregroupBracket {
            residents = List.copyOf(residents);
            upfloaters = List.copyOf(upfloaters);
            pairs = pairs.stream().map(List::copyOf).toList();
        }

        @Override
        public String describe() {
            var pairsText =
                    pairs.stream().map(pair -> pair.get(0) + "-" + pair.get(1)).toList();
            return "bracket " + label + ": residents " + residents + ", upfloaters " + upfloaters + "; pairs "
                    + pairsText + (criteria.isEmpty() ? "" : "; fails " + criteria);
        }
    }

    /** The colour rule that decided a board's colours. */
    record ColourDecision(BoardNumber board, String article) implements TraceStep {
        @Override
        public String describe() {
            return "board " + board + ": colours by [" + article + "]";
        }
    }
}
