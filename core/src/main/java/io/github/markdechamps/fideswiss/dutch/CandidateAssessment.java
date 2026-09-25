package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.Score;
import java.util.List;
import java.util.stream.Stream;

/** A candidate seen in its bracket, with the colours Article 5 would give it. */
final class CandidateAssessment {

    private final Candidate candidate;
    private final Bracket bracket;
    private final RoundToPair round;
    private final Lookahead lookahead;
    private final List<AllocatedPair> games;

    CandidateAssessment(
            Candidate candidate, Bracket bracket, RoundToPair round, Lookahead lookahead, ColourAllocation colours) {
        this.candidate = candidate;
        this.bracket = bracket;
        this.round = round;
        this.lookahead = lookahead;
        this.games = candidate.pairs().stream().map(colours::allocate).toList();
    }

    List<Player> downfloaters() {
        return candidate.downfloaters();
    }

    List<Player> residentDownfloaters() {
        return downfloaters().stream()
                .filter(player -> !bracket.isMovedDown(player))
                .toList();
    }

    List<Player> limbo() {
        return downfloaters().stream().filter(bracket::isMovedDown).toList();
    }

    /** The pairs of the MDP-Pairing: an MDP (S1) against a resident (S2). */
    List<Pair> mdpPairs() {
        return candidate.pairs().stream()
                .filter(pair -> bracket.isMovedDown(pair.s1Player()))
                .toList();
    }

    List<Pair> pairs() {
        return candidate.pairs();
    }

    /** 2017 A.8: the score of the lowest-ranked player of the current bracket. */
    Score lowestScoreInBracket() {
        return bracket.playersInBsnOrder().stream()
                .map(Player::score)
                .min(Score::compareTo)
                .orElseThrow();
    }

    Stream<PlayerInGame> playersInGames() {
        return games.stream()
                .flatMap(game -> Stream.of(
                        new PlayerInGame(game.white(), Colour.WHITE, game.black()),
                        new PlayerInGame(game.black(), Colour.BLACK, game.white())));
    }

    RoundToPair round() {
        return round;
    }

    Lookahead lookahead() {
        return lookahead;
    }

    record PlayerInGame(Player player, Colour colour, Player opponent) {

        boolean getsPreference() {
            return player.colourPreference().isGrantedBy(colour);
        }

        int colourDifferenceAfter() {
            return player.colourDifference() + (colour == Colour.WHITE ? 1 : -1);
        }

        boolean getsSameColourThirdTimeInARow() {
            var colours = player.playedColours();
            return colours.size() >= 2 && colours.getLast() == colour && colours.get(colours.size() - 2) == colour;
        }
    }
}
