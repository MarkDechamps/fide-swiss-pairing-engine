package io.github.markdechamps.fideswiss.cli;

final class Help {

    static final String TEXT = """
            usage:
              fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [settings]
              fide-swiss [--dutch|--dubov|--lim] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
              fide-swiss check <in.trf> [--round <r>]
              fide-swiss standings <in.trf> [--after <r>] [--why <id> <id>]
              fide-swiss version | -r

            settings (override the file's records):
              --system dutch|dubov|lim     the pairing system (also --dutch, --dubov, --lim)
              --maxi-tournament            declare a Lim tournament a Maxi-tournament
              --edition 2026|pre-2026      the Swiss Rules Edition (over 192); pre-2026 is Dutch 2017
              --rounds <n>                 the number of rounds (over 142/XXR)
              --initial-colour white|black the initial colour (over 152/XXC)
              --tiebreaks "<list>"         the Tie-break List (over 202/212)
              --tiebreak-edition 2026-03|2024-08

            "-" as the input reads the TRF from standard input.
            """;

    private Help() {}
}
