package io.github.markdechamps.fideswiss.cli;

final class Help {

    static final String TEXT = """
            usage:
              fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [settings]
              fide-swiss [--dutch|--dubov|--burstein|--lim|--double-swiss|--swiss-team|--olympiad] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
              fide-swiss check <in.trf> [--round <r>]  |  <in.trf> -c [<r>]  |  -check <in.trf>
              fide-swiss standings <in.trf> [--after <r>] [--why <id> <id>]
              fide-swiss generate -o <out%d.trf> [--seed <n>] [--count <k>] [--profile <name>] [--config <cfg>] [--model <in.trf>]
                                  [ranges] [tournament flags]
              fide-swiss -g [<cfg>|<seed>] -o <out.trf> [-s <seed>] [--dutch|--dubov|--burstein|--lim|--double-swiss]
              fide-swiss version | -r

            generator ranges (a number, or A..B drawn per tournament):
              --players --rounds --highest-rating --lowest-rating --unrated <%>
              --forfeit-rate --hpb-rate --zpb-rate --fpb-rate <1 in N>  --withdrawals <%>  --late-entries <%>
              --boards <n|A..B>  the boards of a team match (team systems; the Olympiad has four)
              --draw-percentage <P> (a flat draw share instead of Milvang's model)

            generator tournament flags:
              --profile individual-swiss|accelerated-open|double-swiss|team-swiss|olympiad   the starting settings
              --system dutch|dubov|burstein|lim|double-swiss|swiss-team|olympiad (or --dutch, ..., --swiss-team,
                                                --olympiad): a team system generates team tournaments
              --maxi-tournament (Lim)
              --edition 2026|pre-2026  --tiebreak-edition 2026-03|2024-08
              --acceleration none|baku|random   random: Baku for 20% of the tournaments
              --tiebreaks "<list>"|random       random: 3-5 entries drawn per tournament
              --random-scoring                  3/1/0 or 2/1/0 for 10% of the tournaments
              --random-team-format              Swiss Team: match points 2/1/0 or 3/1/0, MP or GP primary, secondary
                                                score used or not, Type A, B or no colour preferences

            settings (override the file's records):
              --system dutch|dubov|burstein|lim|double-swiss|swiss-team|olympiad  the pairing system (also --dutch,
                                           --dubov, --burstein, --lim, --double-swiss, --swiss-team, --olympiad); a
                                           file with 310 records is a team file, paired by the Swiss Team System
                                           unless its 192 is FIDE_OLYMPIAD (the Olympiad Pairing Rules, D.02);
                                           double-swiss reads two TRF rounds per match (ADR 0007)
              --interpretation <name>=<value>  a Swiss Team or Double-Swiss reading (ADR 0003), repeatable:
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
