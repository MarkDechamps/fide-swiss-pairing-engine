package io.github.markdechamps.fideswiss.tournament;

/**
 * A named, switchable choice between two readings of an ambiguous article, used only where the text literally
 * allows both (ADR 0003). Each has a documented default, the Oracle's reading.
 */
public sealed interface Interpretation
        permits UpfloaterLookAhead,
                LastRoundZeroCdTypeB,
                FloatScore,
                BracketSeating,
                PabValue,
                BakuSecondaryScore,
                EdebtBoardCount {}
