# Changelog

All notable changes to this project are documented here, in the [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format. The project follows Semantic Versioning over its API, its CLI protocol and its output under unchanged settings ([ADR 0008](docs/adr/0008-a-pairing-change-is-a-versioned-change.md)).

Every change that can alter a pairing, a standing or a check verdict is listed under **Pairing changes**, with the article, the systems and editions affected, and any Known Divergence added or closed.

## [Unreleased]

### Added

- The immutable `Tournament` snapshot: participants, recorded rounds, requested byes, withdrawals, late entries (GHR 2.4) and corrections (GHR 4.3), with the `individualSwiss` and `acceleratedOpen` profiles.
- The Dutch System (C.04.3, 2026) with the Basic and General Handling Rules (C.04.1/C.04.2, 2026): the literal bracket procedure of Articles 3–4 with an exact optimum finder, colour allocation (Article 5), board order (GHR 3.6) and a pairing trace.
- The Dubov System (C.04.4.1, 2026): brackets by score, upfloaters by the cheapest perfect matching, the 3.2.4 shifters and transpositions, colour allocation (Article 5); a test-scope Literal Enumerator checks it.
- The Burstein System (C.04.4.2, 2026): seeding rounds by the Dutch System 2026 (1.6), then the Opposition Evaluation Index (Buchholz, Sonneborn-Berger with the 1.7.2 unplayed-round rules), the PAB (3.1), one cheapest perfect matching per bracket holding [C5]–[C8] above the order of 4.3, and Article 5 colours; `--burstein`, `--system burstein`, the generator and the invariant checker; a test-scope Literal Enumerator checks it.
- The Lim System (C.04.4.3, 2026) as a procedure: the 2.2 order with the Median Scoregroup last, floaters chosen so a scoregroup keeps as many pairings as it can, the 3.6–3.8 floater opponents, the generalised Article 4 exchanges, 2.6 cracking, Article 5 colours and Article 7 for round 1; a declared Maxi-tournament (`PairingSystems.lim(MaxiTournament.DECLARED)`); a test-scope Literal Enumerator checks every reachability question.
- Baku and explicit acceleration (C.04.7, 2026) on the Pairing Score, for Dutch, Dubov, Burstein and Lim.
- `Tournament.check(ProposedPairing)`: the Basic Rules' violations, the Dutch System's [C3] (C.3 under the pre-2026 edition), and the differences from the system's pairing.
- Pairing progress (`PairingProgress`), cancellation by interrupt and `RoundPairing.about(id)`.
- The `trf` reader for TRF26 and TRF16 (with `XXR`, `XXC`, `XXA`, `250` and `_BAKU`); record `192` selects Dutch (`FIDE_DUTCH`, `_2026`, `_2025`, `_2017`), Dubov (`FIDE_DUBOV`, `_2026`), Burstein (`FIDE_BURSTEIN`, `_2026`) or Lim (the provisional `FIDE_LIM`, `_2026`).
- The `fide-swiss` command line: `pair` and `-p`, `check`, `-c` and `-check`, `version`, `--explain`, `--quiet`, `--system dutch|dubov|lim` with `--dutch`, `--dubov` and `--lim`, `--maxi-tournament`, and bbp's exit codes plus 6; a portable distribution with launchers.
- The TRF26 writer: `012`, `142`, `152`, `162`, `192` (Dutch, Dutch 2017, Dubov, Burstein, Lim, with `_BAKU`), `212` and the `001` records, read back to the same rounds and settings.
- The Random Tournament Generator (`generator` module): whole Dutch (2026 and pre-2026), Dubov and Lim tournaments from a logged seed, with Milvang's result model, forfeits, requested byes, withdrawals, late entries, and per-tournament Baku, scoring and Tie-break List; JaVaFo/bbp configuration files and model tournaments; the invariant checker on 200 tournaments per system and edition in every build and 5,000 nightly.
- `fide-swiss generate` and the JaVaFo/bbp `-g` form, with a manifest per corpus and exit code 7 when more than 0.1% of the seeds are skipped.

### Pairing changes

- First pairings: Dutch 2026, identical to bbpPairings v6.0.0 in every round of the committed regression corpus and of 647 generated tournaments. Documented readings: 4.4.1, [C5], [C8], [C9], [C18]/[C20] and ADR 0006.
- Check verdicts: under the Dutch System, `check` reports [C3] (2026) or C.3 (pre-2026) when two non-topscorers with the same absolute colour preference meet.
- First pairings: Dubov 2026 (C.04.4.1), no Oracle; identical to its Literal Enumerator in every simulated round, plain and accelerated. Documented readings: 1.8 upfloats, [C9], 4.4.1, the 3.2.4 shifters, ARO rounding, 3.1.4, 5.2.4 and MaxT.
- First pairings: Lim 2026 (C.04.4.3), no Oracle; identical to its Literal Enumerator in every simulated round (plain, Maxi-tournament and accelerated), and no round with a legal pairing is blocked. Documented readings: floater choice, numbering, the Median Scoregroup, compatibility, 3.10, incoming floaters, Article 4, Maxi-tournament, cracking, the PAB, colours, and the median under acceleration. The chesspairing Witness differs widely and never gates, so no Known Divergence is recorded.
- Check verdicts: under Lim, `check` reports 2.1/5.1 when a proposed pair cannot meet within the colour limits; under Dubov, [C3] for the same absolute colour preference.
- Settings: Dubov and Lim reject a pre-2026 Swiss Rules Edition (GHR 1.3); `192` codes other than the listed ones are still rejected.
