package io.github.markdechamps.fideswiss.cli;

final class Help {

    static final String TEXT = """
            usage:
              fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [settings]
              fide-swiss [--dutch] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
              fide-swiss version | -r

            settings (override the file's records):
              --system dutch | --dutch     the pairing system
              --edition 2026|pre-2026      the Swiss Rules Edition (over 192); pre-2026 is Dutch 2017
              --rounds <n>                 the number of rounds (over 142/XXR)
              --initial-colour white|black the initial colour (over 152/XXC)

            "-" as the input reads the TRF from standard input.
            """;

    private Help() {}
}
