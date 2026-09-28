package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A participant's counted rounds, each with its Unplayed Round category (C.07 16.2) where it has one. */
final class ParticipantRounds {

    private final Participant participant;
    private final List<RoundEntry> entries;
    private final List<Optional<UnplayedCategory>> categories;
    private final Points score;

    ParticipantRounds(Participant participant, List<RoundEntry> entries) {
        this.participant = participant;
        this.entries = List.copyOf(entries);
        this.categories = categoriesOf(this.entries);
        this.score = this.entries.stream().map(RoundEntry::points).reduce(Points.ZERO, Points::plus);
    }

    ParticipantId id() {
        return participant.id();
    }

    Participant participant() {
        return participant;
    }

    List<RoundEntry> entries() {
        return entries;
    }

    Points score() {
        return score;
    }

    Optional<UnplayedCategory> categoryOf(int index) {
        return categories.get(index);
    }

    List<RoundEntry> games() {
        return entries.stream().filter(RoundEntry::isGame).toList();
    }

    private static List<Optional<UnplayedCategory>> categoriesOf(List<RoundEntry> entries) {
        var categories = new ArrayList<Optional<UnplayedCategory>>();
        for (var index = 0; index < entries.size(); index++) {
            categories.add(categoryOf(entries, index));
        }
        return List.copyOf(categories);
    }

    private static Optional<UnplayedCategory> categoryOf(List<RoundEntry> entries, int index) {
        return switch (entries.get(index).kind()) {
            case GAME -> Optional.empty();
            case PAIRING_ALLOCATED_BYE, FULL_POINT_BYE -> Optional.of(UnplayedCategory.AWARDED_BYE);
            case FORFEIT_WIN -> Optional.of(UnplayedCategory.FORFEIT_WIN);
            case FORFEIT_LOSS -> Optional.of(UnplayedCategory.FORFEIT_LOSS);
            case HALF_POINT_BYE, ZERO_POINT_BYE, WITHDRAWN, NOT_YET_ENTERED ->
                Optional.of(
                        isFollowedByARoundNotVoluntarilyUnplayed(entries, index)
                                ? UnplayedCategory.REQUESTED_BYE_BEFORE_PLAY
                                : UnplayedCategory.REQUESTED_BYE_TO_THE_END);
        };
    }

    private static boolean isFollowedByARoundNotVoluntarilyUnplayed(List<RoundEntry> entries, int index) {
        return entries.subList(index + 1, entries.size()).stream()
                .anyMatch(later -> !later.kind().isRequestedBye() && later.kind() != RoundEntry.Kind.FORFEIT_LOSS);
    }
}
