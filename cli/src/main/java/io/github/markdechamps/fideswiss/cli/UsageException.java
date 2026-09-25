package io.github.markdechamps.fideswiss.cli;

/** The command line itself is wrong: exit code 3. */
final class UsageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    UsageException(String message) {
        super(message);
    }
}
