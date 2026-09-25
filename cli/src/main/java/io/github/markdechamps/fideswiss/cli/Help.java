package io.github.markdechamps.fideswiss.cli;

final class Help {

    static final String TEXT = """
            usage:
              fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [settings]
              fide-swiss [--dutch] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
              fide-swiss check <in.trf> [--round <r>]  |  <in.trf> -c [<r>]  |  -check <in.trf>
              fide-swiss generate -o <out%d.trf> [--seed <n>] [--count <k>] [--config <cfg>] [ranges]
              fide-swiss -g [<cfg>|<seed>] -o <out.trf> [-s <seed>]
              fide-swiss version | -r

            generator ranges (a number, or A..B drawn per tournament):
              --players --rounds --highest-rating --lowest-rating --unrated <%>
              --forfeit-rate --hpb-rate --zpb-rate --fpb-rate <1 in N>  --withdrawals <%>
              --draw-percentage <P> (a flat draw share instead of Milvang's model)

            settings (override the file's records):
              --system dutch | --dutch     the pairing system
              --rounds <n>                 the number of rounds (over 142/XXR)
              --initial-colour white|black the initial colour (over 152/XXC)

            "-" as the input reads the TRF from standard input.
            """;

    private Help() {}
}
