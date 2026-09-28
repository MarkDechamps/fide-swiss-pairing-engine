package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * One participant's part in a round's pairing, read off the trace: the bracket it was paired in, the brackets it
 * floated down from, its board, opponent and colour with the rule that decided it, or why it was not paired.
 */
public record ParticipantExplanation(
        ParticipantId participant,
        Optional<String> bracket,
        List<String> floatedFrom,
        Optional<BoardNumber> board,
        Optional<ParticipantId> opponent,
        Optional<Colour> colour,
        Optional<String> colourArticle,
        Optional<String> pairingAllocatedByeArticle,
        Optional<Bye> unpaired) {

    public ParticipantExplanation {
        floatedFrom = List.copyOf(floatedFrom);
    }

    static ParticipantExplanation of(RoundPairing pairing, ParticipantId participant) {
        var bracket = Optional.<String>empty();
        var floatedFrom = new ArrayList<String>();
        var colourArticle = Optional.<String>empty();
        var byeArticle = Optional.<String>empty();
        var board = pairing.boards().stream()
                .filter(candidate -> candidate.white().equals(participant)
                        || candidate.black().equals(participant))
                .findFirst();
        for (var step : pairing.trace().steps()) {
            switch (step) {
                case TraceStep.BracketStep bracketStep -> {
                    if (bracketStep.pairs().stream().anyMatch(pair -> pair.contains(participant))) {
                        bracket = Optional.of(bracketStep.label());
                    } else if (bracketStep.downfloaters().contains(participant)) {
                        floatedFrom.add(bracketStep.label());
                    }
                }
                case TraceStep.ColourDecision decision -> {
                    if (board.map(PairedBoard::number)
                            .filter(decision.board()::equals)
                            .isPresent()) {
                        colourArticle = Optional.of(decision.article());
                    }
                }
                case TraceStep.ByeDecision bye -> {
                    if (bye.participant().equals(participant)) {
                        byeArticle = Optional.of(bye.article());
                    }
                }
            }
        }
        return new ParticipantExplanation(
                participant,
                bracket,
                floatedFrom,
                board.map(PairedBoard::number),
                board.map(found -> found.white().equals(participant) ? found.black() : found.white()),
                board.map(found -> found.white().equals(participant) ? Colour.WHITE : Colour.BLACK),
                colourArticle,
                byeArticle,
                Optional.ofNullable(pairing.unpaired().get(participant)));
    }

    public String describe() {
        var lines = new ArrayList<String>();
        unpaired.ifPresent(reason -> lines.add(
                participant + " is not paired: " + reason.name().toLowerCase().replace('_', ' ')));
        floatedFrom.forEach(label -> lines.add(participant + " floated down from bracket " + label));
        pairingAllocatedByeArticle.ifPresent(
                article -> lines.add(participant + " receives the pairing-allocated bye [" + article + "]"));
        opponent.ifPresent(other -> lines.add(participant + " is paired with " + other
                + bracket.map(label -> " in bracket " + label).orElse("")
                + board.map(number -> " on board " + number).orElse("")));
        colour.ifPresent(
                allocated -> lines.add(participant + " has " + allocated.name().toLowerCase()
                        + colourArticle.map(article -> " [" + article + "]").orElse("")));
        return String.join(System.lineSeparator(), lines);
    }
}
