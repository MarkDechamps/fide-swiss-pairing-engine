package io.github.markdechamps.fideswiss.tiebreak;

/** The Swiss categories of Unplayed Rounds (C.07 16.2). */
enum UnplayedCategory {
    /** 16.2.1: pairing-allocated byes and full-point byes. */
    AWARDED_BYE("16.2.1"),
    /** 16.2.2: forfeit wins. */
    FORFEIT_WIN("16.2.2"),
    /** 16.2.3: requested byes followed by at least one round that is not voluntarily unplayed. */
    REQUESTED_BYE_BEFORE_PLAY("16.2.3"),
    /** 16.2.4: forfeit losses. */
    FORFEIT_LOSS("16.2.4"),
    /** 16.2.5: requested byes followed only by voluntary unplayed rounds, or in the last round. */
    REQUESTED_BYE_TO_THE_END("16.2.5");

    private final String article;

    UnplayedCategory(String article) {
        this.article = article;
    }

    /** A Voluntary Unplayed Round: a requested bye or a forfeit loss (16.1.2). */
    boolean isVoluntary() {
        return this == REQUESTED_BYE_BEFORE_PLAY || this == FORFEIT_LOSS || this == REQUESTED_BYE_TO_THE_END;
    }

    boolean isForfeit() {
        return this == FORFEIT_WIN || this == FORFEIT_LOSS;
    }

    String article() {
        return "C.07 " + article;
    }
}
