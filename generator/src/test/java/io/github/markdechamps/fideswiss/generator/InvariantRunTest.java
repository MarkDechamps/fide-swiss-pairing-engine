package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The invariant run: seeded tournaments of every implemented system and Swiss Rules Edition must break no absolute
 * criterion. The PR gate plays 200 of each from a fixed corpus seed; the nightly job raises {@code fideswiss.invariant.tournaments} to
 * 5,000 with a rotating {@code fideswiss.invariant.seed}, and a release gate to 50,000 (Verification strategy).
 */
class InvariantRunTest {

    private static final CorpusSeed CORPUS = CorpusSeed.of(Long.getLong("fideswiss.invariant.seed", 20260925L));
    private static final int TOURNAMENTS = Integer.getInteger("fideswiss.invariant.tournaments", 200);

    /** Every system with the editions it has a text for: Dubov and Lim exist only in 2026. */
    enum PlayedSystem {
        DUTCH_2026(PairingSystems.dutch(), SwissRulesEdition.EDITION_2026),
        DUTCH_2017(PairingSystems.dutch(), SwissRulesEdition.PRE_2026),
        DUBOV_2026(PairingSystems.dubov(), SwissRulesEdition.EDITION_2026),
        LIM_2026(PairingSystems.lim(), SwissRulesEdition.EDITION_2026);

        private final PairingSystem system;
        private final SwissRulesEdition edition;

        PlayedSystem(PairingSystem system, SwissRulesEdition edition) {
            this.system = system;
            this.edition = edition;
        }

        TournamentSettings settings() {
            return Profiles.individualSwiss(NumberOfRounds.of(9)).with(system).with(edition);
        }
    }

    @ParameterizedTest
    @EnumSource(PlayedSystem.class)
    void generatedTournamentsKeepEveryInvariant(PlayedSystem played) {
        var settings = GeneratorSettings.of(played.settings())
                .withPlayers(Range.of(14, 48))
                .withRounds(Range.of(5, 11))
                .withForfeitRate(Range.of(5, 30))
                .withHalfPointByeRate(Range.of(10, 200))
                .withZeroPointByeRate(Range.of(10, 200))
                .withFullPointByeRate(Range.of(20, 200))
                .withWithdrawalPercentage(Range.of(0, 8))
                .withRandomAcceleration()
                .withRandomScoring()
                .withRandomTieBreaks();
        var generator = TournamentGenerator.of(settings);
        var violations = new ArrayList<String>();
        var skipped = 0;
        for (var index = 0; index < TOURNAMENTS; index++) {
            var seed = CORPUS.tournament(index);
            switch (generator.generate(seed)) {
                case GeneratedTournament.Completed completed ->
                    InvariantChecker.violationsOf(completed.tournament())
                            .forEach(violation -> violations.add("seed " + seed + " " + violation));
                case GeneratedTournament.Skipped skip -> skipped++;
            }
        }

        assertThat(violations).isEmpty();
        assertThat(skipped).isLessThanOrEqualTo(TOURNAMENTS / 100);
    }
}
