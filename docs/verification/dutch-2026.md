# Dutch System 2026: traceability

Every article of [FIDE Handbook C.04.3, Dutch System, effective 1 February 2026](https://handbook.fide.com/chapter/C0403202602), and the tests that cite it. This covers items 1 and 2 of the definition of done in the Verification strategy (`.wayfinder/fide-swiss-engine/10-verification-strategy.md`): every article has at least one test that cites it, and every Handbook worked example passes.

Each test states the article it traces in a comment beside its assertion. The expected pairings were worked out by hand from the text, following the library's documented readings (README, *Readings of the Dutch 2026 text*).

Test classes live in `core/src/test/java/io/github/markdechamps/fideswiss/dutch/`:

| Class | What it checks |
|---|---|
| `DutchDefinitionsTest` | Article 1 definitions, on `Player`, `Bracket`, the float rule and `RoundToPair` |
| `DutchQualityCriteriaTest` | the failure one hand-built candidate scores on each of [C5]–[C21] |
| `DutchPairingCriteriaTest` | whole rounds through `Tournament.pairNextRound` whose pairing only the cited criterion explains |
| `DutchBracketProcedureTest` | Article 3 and 1.9, through `pairNextRound` and the pairing trace |
| `DutchGenerationOrderTest` | Article 4 and its worked examples, on `Transpositions`, `ResidentExchange` and `MdpSets` |
| `DutchColourAllocationTest` | Article 5, one pair at a time on `ColourAllocation` |
| `DutchRoundOneTest`, `DutchLaterRoundsTest`, `DutchCheckTest` | the earlier round-one, later-round and `check` tests |

**Supplementary evidence:** `trf/src/test/java/io/github/markdechamps/fideswiss/trf/DutchRegressionCorpusTest#pairsEveryRoundAsBbpPairingsV6Did` replays the bbpPairings v6.0.0 corpus (`trf/src/test/resources/corpus/dutch-2026`) and must match the Oracle in every round. That corpus is the evidence for how all the articles work together. The table below lists only the tests that cite each article directly.

## Article 0 – Terms

| Article | Tests |
|---|---|
| 0 Terms and definitions (refers to the Handbook's general Swiss terms) | Not tested here: it has no rule of its own. Its terms are used in C.04.1/C.04.2 and in the tests below. |

## Article 1 – Introductory remarks and definitions

| Article | Tests |
|---|---|
| 1.1 Tournament Pairing Number | `DutchRoundOneTest#pairsTheTopHalfAgainstTheBottomHalfWithColoursByPairingNumberParity` (Pairing Numbers follow rating; the numbering itself is C.04.2 2.2–2.4, see `LateEntryTest`, `CorrectionTest`) |
| 1.2 Order | `DutchDefinitionsTest.OrderAndBrackets#ranksPlayersByScoreThenByPairingNumber`; `DutchColourAllocationTest.TheHigherRankedPlayer#isRankedByScoreBeforePairingNumber` |
| 1.3.1 Scoregroup | `DutchDefinitionsTest.OrderAndBrackets#groupsPlayersOfTheSameScoreIntoScoregroupsFromTheTopDown` |
| 1.3.2 Pairing bracket | `DutchDefinitionsTest.OrderAndBrackets#callsABracketHeterogeneousOnlyWhenPlayersMovedDownIntoIt`; `DutchBracketProcedureTest#pairsTheBracketsFromTheTopScoregroupDown` |
| 1.3.3 Homogeneous / heterogeneous | `DutchDefinitionsTest.OrderAndBrackets#callsABracketHeterogeneousOnlyWhenPlayersMovedDownIntoIt` |
| 1.3.4 Remainder | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket` |
| 1.4.1 Downfloaters and MDPs | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket`, `#leavesAnMdpWhoCannotBePairedInTheLimboAndTakesTheNextMdpSet`; `DutchLaterRoundsTest#pairsScoregroupsFromTheTopDownAndAlternatesColours` |
| 1.4.2 Upfloat / downfloat | `DutchDefinitionsTest.Floats#giveTheHigherScoredPlayerOfAGameADownfloatAndTheOtherAnUpfloat` |
| 1.4.3 Downfloat for the PAB or for more than a loss without playing | `DutchDefinitionsTest.Floats#giveADownfloatForThePairingAllocatedBye`, `#giveADownfloatForMoreThanALossWithoutPlaying` |
| 1.4.4 No other floats | `DutchDefinitionsTest.Floats#giveNoOtherFloat` |
| 1.5 Pairing-allocated bye | `DutchRoundOneTest#givesThePairingAllocatedByeToTheLowestRankedWhenTheNumberIsOdd`; `DutchPairingCriteriaTest.TheByeCriteria#giveTheByeToThePlayerWithFewerUnplayedGames` |
| 1.6 Colour difference | `DutchDefinitionsTest.ColourPreferences#measureTheColourDifferenceAsWhitesMinusBlacks` |
| 1.7.1 Absolute colour preference | `DutchDefinitionsTest.ColourPreferences#areAbsoluteBeyondAColourDifferenceOfOne`, `#areAbsoluteAfterTheSameColourInTheTwoLatestRoundsPlayed` |
| 1.7.2 Strong colour preference | `DutchDefinitionsTest.ColourPreferences#areStrongAtAColourDifferenceOfOneForTheColourThatReducesIt` |
| 1.7.3 Mild colour preference | `DutchDefinitionsTest.ColourPreferences#areMildAtAColourDifferenceOfZeroForTheColourThatAlternates` |
| 1.7.4 No colour preference | `DutchDefinitionsTest.ColourPreferences#doNotExistBeforeTheFirstGamePlayed`; `DutchColourAllocationTest.BothPreferences#areGrantedWhenOnlyOnePlayerHasOne` |
| 1.8 Topscorers | `DutchDefinitionsTest.Topscorers#scoreOverHalfTheMaximumWhenTheFinalRoundIsPaired`, `#doNotExistBeforeTheFinalRound`; `DutchCheckTest#letsTopscorersWithTheSameAbsolutePreferenceMeetInTheLastRound` |
| 1.9.1 Complete round-pairing | `DutchBracketProcedureTest.GivenAHomogeneousBracket#floatsThePlayerLeftOverWhenItsSizeIsOdd`; `DutchRoundOneTest#givesThePairingAllocatedByeToTheLowestRankedWhenTheNumberIsOdd` |
| 1.9.2 Top scoregroup down | `DutchBracketProcedureTest#pairsTheBracketsFromTheTopScoregroupDown`; `DutchLaterRoundsTest#pairsScoregroupsFromTheTopDownAndAlternatesColours` |
| 1.9.3 No complete pairing: the Chief Arbiter decides | `DutchPairingCriteriaTest.TheAbsoluteCriteria#leaveTheChiefArbiterToDecideWhenNoPairingMeetsThem` |

## Article 2 – Pairing criteria

| Article | Tests |
|---|---|
| 2.1.1 [C1] No second meeting | `DutchPairingCriteriaTest.TheAbsoluteCriteria#neverPairTwoPlayersWhoHaveMet`, `#barAWinWithoutPlayingFromThePairingAllocatedBye` (a forfeit is no meeting, GHR 3.5) |
| 2.1.2 [C2] No second PAB, nor after a win without playing | `DutchPairingCriteriaTest.TheAbsoluteCriteria#giveThePairingAllocatedByeOnlyToAPlayerWhoHasNotHadOne`; `#barAWinWithoutPlayingFromThePairingAllocatedBye`; `PairingCheckTest.GivenASecondPairingAllocatedBye` (C.04.1 Art. 4 in `check`) |
| 2.1.3 [C3] Non-topscorers with the same absolute preference | `DutchPairingCriteriaTest.TheAbsoluteCriteria#neverPairTwoNonTopscorersWithTheSameAbsolutePreference`, `#leaveTheChiefArbiterToDecideWhenNoPairingMeetsThem`; `DutchCheckTest` (all three) |
| 2.2.1 [C4] Completion | `DutchPairingCriteriaTest.TheCompletionCriterion#floatsABracketThatCouldPairItselfWhenTheRoundCouldNotBeCompletedOtherwise` |
| 2.3.1 [C5] Lowest PAB score | `DutchPairingCriteriaTest.TheAbsoluteCriteria#giveThePairingAllocatedByeOnlyToAPlayerWhoHasNotHadOne`, `DutchPairingCriteriaTest.TheByeCriteria#giveTheByeToThePlayerWithFewerUnplayedGames`; `DutchQualityCriteriaTest#comeInTheirPriorityOrder`. The reading that [C5] is fixed before the first bracket, even when that costs an upper bracket extra downfloaters, is backed only by the corpus. |
| 2.4.1 [C6] Fewest downfloaters | `DutchQualityCriteriaTest.Downfloaters#areCountedByC6`; `DutchPairingCriteriaTest.TheAbsoluteCriteria#neverPairTwoPlayersWhoHaveMet` |
| 2.4.2 [C7] Lowest downfloater scores | `DutchQualityCriteriaTest.Downfloaters#haveTheirScoresComparedInDescendingOrderByC7`; `DutchGenerationOrderTest.MovedDownPlayerSets#areValidOnlyWhenTheLimboTheyLeaveCompliesWithC7` |
| 2.4.3 [C8] The following bracket | `DutchPairingCriteriaTest.TheQualityCriteria#floatThePlayerWithWhomTheFollowingBracketPairsBest` |
| 2.4.4 [C9] Fewest unplayed games of the PAB assignee | `DutchQualityCriteriaTest.UnplayedGamesOfThePairingAllocatedByeAssignee` (all three); `DutchPairingCriteriaTest.TheByeCriteria#giveTheByeToThePlayerWithFewerUnplayedGames` |
| 2.4.5 [C10] Topscorers beyond ±2 | `DutchQualityCriteriaTest.TopscorersColours#countAColourDifferenceBeyondTwoInC10`, `#countNothingBeforeTheFinalRound` |
| 2.4.6 [C11] Topscorers with the same colour three times | `DutchQualityCriteriaTest.TopscorersColours#countTheSameColourAThirdTimeInARowInC11`, `#countNothingBeforeTheFinalRound` |
| 2.4.7 [C12] Colour preferences | `DutchQualityCriteriaTest.ColourPreferences#countEveryPlayerNotGettingItsPreferenceInC12`; `DutchPairingCriteriaTest.TheQualityCriteria#grantColourPreferencesOverTheFirstTransposition` |
| 2.4.8 [C13] Strong colour preferences | `DutchQualityCriteriaTest.ColourPreferences#countStrongPreferencesNotGrantedAgainInC13`, `#countEveryPlayerNotGettingItsPreferenceInC12` |
| 2.4.9 [C14] Resident downfloaters, previous round | `DutchQualityCriteriaTest.Floats#countResidentDownfloatersWhoDownfloatedOneOrTwoRoundsBefore`, `#doNotCountAnMdpFloatingOnAsAResidentDownfloater` |
| 2.4.10 [C15] MDP opponents, upfloat previous round | `DutchQualityCriteriaTest.Floats#countMdpOpponentsWhoUpfloatedOneOrTwoRoundsBefore` |
| 2.4.11 [C16] Resident downfloaters, two rounds before | `DutchQualityCriteriaTest.Floats#countResidentDownfloatersWhoDownfloatedOneOrTwoRoundsBefore` |
| 2.4.12 [C17] MDP opponents, upfloat two rounds before | `DutchQualityCriteriaTest.Floats#countMdpOpponentsWhoUpfloatedOneOrTwoRoundsBefore` |
| 2.4.13 [C18] Score differences of MDPs, downfloat previous round | `DutchQualityCriteriaTest.ScoreDifferences#ofMdpsWhoDownfloatedAreComparedInC18AndC20`, `#ofAnMdpFloatingOnExceedAnyResidentsAndGrowWithItsScore` |
| 2.4.14 [C19] Score differences of MDP opponents, upfloat previous round | `DutchQualityCriteriaTest.ScoreDifferences#ofMdpOpponentsWhoUpfloatedAreComparedInC19AndC21` |
| 2.4.15 [C20] As [C18], two rounds before | `DutchQualityCriteriaTest.ScoreDifferences#ofMdpsWhoDownfloatedAreComparedInC18AndC20` |
| 2.4.16 [C21] As [C19], two rounds before | `DutchQualityCriteriaTest.ScoreDifferences#ofMdpOpponentsWhoUpfloatedAreComparedInC19AndC21` |

## Article 3 – Pairing process for a bracket

| Article | Tests |
|---|---|
| 3.1.1 M0 | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket` (M0 = 1), `#leavesAnMdpWhoCannotBePairedInTheLimboAndTakesTheNextMdpSet` (M0 = 2) |
| 3.1.2 MaxPairs | `DutchBracketProcedureTest.GivenAHomogeneousBracket#floatsThePlayerLeftOverWhenItsSizeIsOdd`; `DutchPairingCriteriaTest.TheCompletionCriterion#floatsABracketThatCouldPairItselfWhenTheRoundCouldNotBeCompletedOtherwise` (MaxPairs 0 under [C4]) |
| 3.1.3 M1 | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#leavesAnMdpWhoCannotBePairedInTheLimboAndTakesTheNextMdpSet` |
| 3.2.1 Subgroups S1 and S2 | `DutchRoundOneTest#pairsTheTopHalfAgainstTheBottomHalfWithColoursByPairingNumberParity` |
| 3.2.2 S1 | `DutchRoundOneTest#pairsTheTopHalfAgainstTheBottomHalfWithColoursByPairingNumberParity`; `DutchBracketProcedureTest.GivenAHomogeneousBracket#floatsThePlayerLeftOverWhenItsSizeIsOdd`, `GivenAHeterogeneousBracket#pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket` |
| 3.2.3 S2 | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket` |
| 3.2.4 Limbo | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#leavesAnMdpWhoCannotBePairedInTheLimboAndTakesTheNextMdpSet` |
| 3.3.1 S1[i] against S2[i] | `DutchRoundOneTest#pairsTheTopHalfAgainstTheBottomHalfWithColoursByPairingNumberParity`; `DutchBracketProcedureTest.GivenAHomogeneousBracket#acceptsTheFirstPerfectCandidate` |
| 3.3.2 Homogeneous candidate | `DutchBracketProcedureTest.GivenAHomogeneousBracket#floatsThePlayerLeftOverWhenItsSizeIsOdd`; `DutchPairingCriteriaTest.TheCompletionCriterion#floatsABracketThatCouldPairItselfWhenTheRoundCouldNotBeCompletedOtherwise` |
| 3.3.3 MDP-Pairing and remainder | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket` |
| 3.3.4 Heterogeneous candidate with the Limbo | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#leavesAnMdpWhoCannotBePairedInTheLimboAndTakesTheNextMdpSet` |
| 3.4.1 Perfect candidate | `DutchBracketProcedureTest.GivenAHomogeneousBracket#acceptsTheFirstPerfectCandidate` |
| 3.5.1–3.5.3 Alterations | `DutchPairingCriteriaTest.TheQualityCriteria#grantColourPreferencesOverTheFirstTransposition` (candidates altered until one is perfect) |
| 3.6.1 Transpositions, then resident exchanges | `DutchPairingCriteriaTest.TheAbsoluteCriteria#neverPairTwoPlayersWhoHaveMet` (transposition), `TheQualityCriteria#grantColourPreferencesOverTheFirstTransposition` (exchange); `DutchGenerationOrderTest.ResidentExchanges#swapEquallySizedGroupsBetweenTheOriginalSubgroupsAndReSortThem` |
| 3.7.1 Remainder as a homogeneous bracket | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket` |
| 3.7.2 Next transposition of S2 | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket` |
| 3.7.3 Next MDP set | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#leavesAnMdpWhoCannotBePairedInTheLimboAndTakesTheNextMdpSet` |
| 3.8.1 Best candidate, earliest on a tie | `DutchBracketProcedureTest.GivenAHeterogeneousBracket#takesTheEarliestBestCandidateWhenNoneIsPerfect`; `DutchQualityCriteriaTest#comeInTheirPriorityOrder` |

## Article 4 – Sequential generation of candidates

| Article | Tests |
|---|---|
| 4.1.1 In-bracket sequence numbers | `DutchGenerationOrderTest.InBracketSequenceNumbers#numberTheMovedDownPlayersFirstThenTheResidentsInPairingOrder` |
| 4.2.1 Transposition | `DutchGenerationOrderTest.TranspositionsOfS2#changeOnlyTheOrderOfS2AndLeaveTheRestInBsnOrder` |
| 4.2.2 Lexicographic order, first N1 BSNs (**worked examples**) | `DutchGenerationOrderTest.TranspositionsOfS2#giveAnElevenPlayerHomogeneousBracket720InLexicographicOrderOfTheFirstFiveBsns`, `#giveAnElevenPlayerBracketWithTwoMdps72OrdersOfTheResidentsTheyMeet` |
| 4.3.1 Resident exchange | `DutchGenerationOrderTest.ResidentExchanges#swapEquallySizedGroupsBetweenTheOriginalSubgroupsAndReSortThem` |
| 4.3.2 The four comparison rules | `DutchGenerationOrderTest.ResidentExchanges#comeInTheOrderOfTheFourComparisonRules` |
| 4.4.1 Valid MDP sets | `DutchGenerationOrderTest.MovedDownPlayerSets#areValidOnlyWhenTheLimboTheyLeaveCompliesWithC7`, `#areJudgedOnTheLimboThatCanActuallyBeAchieved` (documented reading) |
| 4.4.2 Smallest differing BSN | `DutchGenerationOrderTest.MovedDownPlayerSets#comeInOrderOfTheirSmallestDifferingBsn` |
| 4.5.1 Next element | Every ordering test in `DutchGenerationOrderTest`; `DutchBracketProcedureTest.GivenAHeterogeneousBracket#leavesAnMdpWhoCannotBePairedInTheLimboAndTakesTheNextMdpSet` |

## Article 5 – Colour allocation

| Article | Tests |
|---|---|
| 5.1 Initial colour | `DutchColourAllocationTest.TheHigherRankedPlayer#takesTheInitialColourWithAnOddPairingNumber` (White and Black drawn) |
| 5.2.1 Both preferences | `DutchColourAllocationTest.BothPreferences#areGrantedWhenTheyDiffer`, `#areGrantedWhenOnlyOnePlayerHasOne` |
| 5.2.2 The stronger preference; the wider difference for two absolutes | `DutchColourAllocationTest.TheStrongerPreference#isGrantedWhenBothWantTheSameColour`, `#goesToTheWiderColourDifferenceWhenBothAreAbsolute` |
| 5.2.3 Alternate to the most recent differing round (GHR 3.4) | `DutchColourAllocationTest.Alternation#followsTheMostRecentRoundInWhichTheColoursDiffered`, `#alignsTheLatestPlayedGamesSoUnplayedRoundsCountAsTheEarliest` |
| 5.2.4 The higher-ranked player's preference | `DutchColourAllocationTest.TheHigherRankedPlayer#getsItsOwnPreference`, `#isRankedByScoreBeforePairingNumber` |
| 5.2.5 Odd Pairing Number takes the initial colour | `DutchColourAllocationTest.TheHigherRankedPlayer#takesTheInitialColourWithAnOddPairingNumber`, `#takesTheOppositeColourWithAnEvenPairingNumber`; `DutchRoundOneTest#pairsTheTopHalfAgainstTheBottomHalfWithColoursByPairingNumberParity` |

## Handbook worked examples

C.04.3 (2026) has two worked examples, both in the note to 4.2.2. Both pass:

- **11-player homogeneous bracket, 720 transpositions:** count, first, boundary and last elements.
- **11-player heterogeneous bracket with two MDPs, 72 transpositions:** count, first, boundary and last elements.

The text gives no worked example for 4.3.2 or 4.4.2. The exchange order is checked on an S1 of 1–3 and an S2 of 4–7 worked out by hand. The MDP-set order is checked on the `{1,3} < {1,4} < {3,4}` illustration from the commentary [ANN p.44–45].

**A slip in the text.** The 4.2.2 note lists `6-7-8-10-11` as the third transposition. Strict lexicographic order, which 4.2.2 prescribes and which the stated count of 720 implies, puts `6-7-8-10-9` third, and the test asserts that. The same slip is in the pre-2026 text. It was already noted in the Readable Dutch algorithm ticket (`11-dutch-algorithm.md`).

## Articles with no direct test

None. The one partial point is [C5]: the reading that the lowest reachable PAB score outranks an upper bracket's [C6] is covered only by the bbpPairings v6 corpus. No hand-sized round was found in which [C5] alone, and not [C4] or [C8], makes that difference.
