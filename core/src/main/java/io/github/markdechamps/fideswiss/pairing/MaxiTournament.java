package io.github.markdechamps.fideswiss.pairing;

/**
 * Whether the organiser declared a Lim tournament a Maxi-tournament, under which floater choices and exchanges for
 * colour are allowed only between players rated within 100 points (C.04.4.3 3.2.3, 3.8, 5.7). It is never inferred
 * from the size of the field.
 */
public enum MaxiTournament {
    NOT_DECLARED,
    DECLARED
}
