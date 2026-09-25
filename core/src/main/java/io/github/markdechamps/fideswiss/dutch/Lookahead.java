package io.github.markdechamps.fideswiss.dutch;

import java.util.List;
import java.util.Optional;

/** What a bracket needs to know about the brackets below it ([C5], [C8], [C9]). */
interface Lookahead {

    boolean isLastBracket();

    Optional<Player> pairingAllocatedByeAssignee(List<Player> downfloaters);

    /** [C8]: how well the following bracket can meet [C6] and [C7] with these downfloaters. */
    Failure followingBracketOutcome(List<Player> downfloaters);
}
