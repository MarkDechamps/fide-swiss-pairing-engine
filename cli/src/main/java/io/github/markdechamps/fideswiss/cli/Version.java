package io.github.markdechamps.fideswiss.cli;

/** {@code fide-swiss version}: the library version and every implemented system with its status (GHR 1.3). */
final class Version {

    private Version() {}

    static String describe() {
        return "fide-swiss " + number() + "\n"
                + "  C.04.3 Dutch System 2026 (with C.04.1/C.04.2 2026): experimental\n"
                + "  C.04.4.1 Dubov System 2026: experimental\n"
                + "  C.04.4.3 Lim System 2026: experimental\n";
    }

    static String number() {
        var descriptor = Main.class.getModule().getDescriptor();
        return descriptor == null ? "development" : descriptor.rawVersion().orElse("development");
    }
}
