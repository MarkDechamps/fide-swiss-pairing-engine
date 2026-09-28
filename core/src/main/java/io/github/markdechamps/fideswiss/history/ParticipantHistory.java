package io.github.markdechamps.fideswiss.history;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Score;
import java.util.ArrayList;
import java.util.List;

/** One participant's rounds so far, and the facts every system derives from them. */
public final class ParticipantHistory {

    private final ParticipantId participant;
    private final List<RoundRecord> records;
    private final Points winValue;

    ParticipantHistory(ParticipantId participant, List<RoundRecord> records, Points winValue) {
        this.participant = participant;
        this.records = List.copyOf(records);
        this.winValue = winValue;
    }

    public ParticipantId participant() {
        return participant;
    }

    public List<RoundRecord> records() {
        return records;
    }

    public RoundRecord recordOf(RoundNumber round) {
        return records.get(round.value() - 1);
    }

    public Score score() {
        return scoreAfter(records.size());
    }

    /** The score before the given round was played. */
    public Score scoreBefore(RoundNumber round) {
        return scoreAfter(round.value() - 1);
    }

    /** C.04.1 Art. 2 with GHR 3.5: only a game actually played counts as having met. */
    public boolean hasPlayedAgainst(ParticipantId opponent) {
        return records.stream()
                .filter(RoundRecord::isPlayed)
                .anyMatch(record -> record.pairedOpponent().orElseThrow().equals(opponent));
    }

    /**
     * C.04.1 Art. 4: no PAB for a participant who already had one, or who scored a win's points in one round
     * without playing.
     */
    public boolean mayReceivePairingAllocatedBye() {
        return mayReceivePairingAllocatedBye(PairingAllocatedByeBar.BASIC_RULES_2026);
    }

    /** The same under the PAB bar of the given edition of the Basic Rules. */
    public boolean mayReceivePairingAllocatedBye(PairingAllocatedByeBar bar) {
        return records.stream().noneMatch(record -> bar.bars(record, winValue));
    }

    /** The colours of the games actually played, in order; unplayed rounds drop out (GHR 3.4). */
    public List<Colour> playedColours() {
        var colours = new ArrayList<Colour>();
        for (var record : records) {
            if (record instanceof RoundRecord.Game game) {
                colours.add(game.colour());
            }
        }
        return List.copyOf(colours);
    }

    public int unplayedRounds() {
        return (int) records.stream().filter(record -> !record.isPlayed()).count();
    }

    private Score scoreAfter(int rounds) {
        var score = Score.ZERO;
        for (var index = 0; index < rounds; index++) {
            score = score.plus(records.get(index).points());
        }
        return score;
    }
}
