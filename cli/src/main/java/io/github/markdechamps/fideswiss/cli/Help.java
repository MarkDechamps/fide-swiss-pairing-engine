package io.github.markdechamps.fideswiss.cli;

final class Help {

    static final String TEXT = """
            usage:
              fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [settings]
              fide-swiss [--dutch|--dubov|--lim|--swiss-team] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
              fide-swiss check <in.trf> [--round <r>]  |  <in.trf> -c [<r>]  |  -check <in.trf>
              fide-swiss standings <in.trf> [--after <r>] [--why <id> <id>]
              fide-swiss generate -o <out%d.trf> [--seed <n>] [--count <k>] [--profile <name>] [--config <cfg>] [--model <in.trf>]
                                  [ranges] [tournament flags]
              fide-swiss -g [<cfg>|<seed>] -o <out.trf> [-s <seed>] [--dutch|--dubov|--lim]
              fide-swiss version | -r

            generator ranges (a number, or A..B drawn per tournament):
              --players --rounds --highest-rating --lowest-rating --unrated <%>
              --forfeit-rate --hpb-rate --zpb-rate --fpb-rate <1 in N>  --withdrawals <%>  --late-entries <%>
              --draw-percentage <P> (a flat draw share instead of Milvang's model)

            generator tournament flags:
              --profile individual-swiss|accelerated-open   the starting settings
              --system dutch|dubov|lim (or --dutch, --dubov, --lim)  --maxi-tournament (Lim)
              --edition 2026|pre-2026  --tiebreak-edition 2026-03|2024-08
              --acceleration none|baku|random   random: Baku for 20% of the tournaments
              --tiebreaks "<list>"|random       random: 3-5 entries drawn per tournament
              --random-scoring                  3/1/0 or 2/1/0 for 10% of the tournaments

            settings (override the file's records):
              --system dutch|dubov|lim|swiss-team  the pairing system (also --dutch, --dubov, --lim,
                                           --swiss-team); a file with 310 records is a team file, paired by
                                           the Swiss Team System
              --interpretation <name>=<value>  a Swiss Team reading (ADR 0003), repeatable:
                  upfloater-look-ahead=parity-minimum|graded, last-round-zero-cd-type-b=strong|none,
                  float-score=pairing|real
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
