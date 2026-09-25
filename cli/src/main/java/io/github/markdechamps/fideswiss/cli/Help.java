package io.github.markdechamps.fideswiss.cli;

final class Help {

    static final String TEXT = """
            usage:
              fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [settings]
              fide-swiss [--dutch|--dubov] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
              fide-swiss version | -r

            settings (override the file's records):
              --system dutch|dubov         the pairing system (also --dutch, --dubov)
              --rounds <n>                 the number of rounds (over 142/XXR)
              --initial-colour white|black the initial colour (over 152/XXC)

            "-" as the input reads the TRF from standard input.
            """;

    private Help() {}
}
