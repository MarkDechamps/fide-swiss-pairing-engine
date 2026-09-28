package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.math.BigDecimal;
import java.util.List;

/** The ratings of the opponents met over the board, one term per game: the terms of ARO (C.07 10.1). */
final class OpponentRatings implements TermSource {

    @Override
    public List<Term> termsOf(ParticipantId participant, TieBreakContext context) {
        return context.of(participant).games().stream()
                .map(game -> {
                    var opponent = game.opponent().orElseThrow();
                    var rating = context.of(opponent).participant().rating().valueOrZero();
                    return Term.of(game.round(), opponent.value(), BigDecimal.valueOf(rating), false, List.of());
                })
                .toList();
    }
}
