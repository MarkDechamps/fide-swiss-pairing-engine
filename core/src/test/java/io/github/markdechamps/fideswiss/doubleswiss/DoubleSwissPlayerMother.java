package io.github.markdechamps.fideswiss.doubleswiss;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Players as the Top-Scoregroup Procedure sees them, identified by their TPN. */
final class DoubleSwissPlayerMother {

    private DoubleSwissPlayerMother() {}

    /** A player with no colours yet, who has met the given TPNs. */
    static Contender player(int tpn, String score, int... met) {
        return player(tpn, score, List.of(), met(met), false, false);
    }

    /** A player with the given played-only colour history, nobody met. */
    static Contender withColours(int tpn, String score, Colour... colours) {
        return player(tpn, score, List.of(colours), Set.of(), false, false);
    }

    static Contender player(
            int tpn, String score, List<Colour> colours, Set<ParticipantId> met, boolean barred, boolean floated) {
        return new Contender(
                id(tpn), tpn, Points.of(score), Points.of(score), colours.size(), colours, met, barred, floated);
    }

    static Set<ParticipantId> met(int... tpns) {
        return Arrays.stream(tpns).mapToObj(DoubleSwissPlayerMother::id).collect(Collectors.toSet());
    }

    static ParticipantId id(int tpn) {
        return ParticipantId.of(String.valueOf(tpn));
    }
}
