package io.github.markdechamps.fideswiss.cli;

final class Help {

    static final String TEXT = """
            usage:
              fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [settings]
              fide-swiss [--dutch|--swiss-team] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
              fide-swiss check <in.trf> [--round <r>]
              fide-swiss standings <in.trf> [--after <r>] [--why <id> <id>]
              fide-swiss version | -r

            settings (override the file's records):
              --system dutch|swiss-team    the pairing system, also --dutch, --swiss-team; a file with
                                           310 records is a team file, paired by the Swiss Team System
              --interpretation <name>=<value>  a Swiss Team reading (ADR 0003), repeatable:
                  upfloater-look-ahead=parity-minimum|graded, last-round-zero-cd-type-b=strong|none,
                  float-score=pairing|real
              --edition 2026|pre-2026      the Swiss Rules Edition (over 192); pre-2026 is Dutch 2017
              --rounds <n>                 the number of rounds (over 142/XXR)
              --initial-colour white|black the initial colour (over 152/XXC)
              --tiebreaks "<list>"         the Tie-break List (over 202/212)
              --tiebreak-edition 2026-03|2024-08

            "-" as the input reads the TRF from standard input.
            """;

    private Help() {}
}
