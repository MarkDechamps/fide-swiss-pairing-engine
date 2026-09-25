package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.RoundPairing;

/**
 * JaVaFo's Pairing Reply: the number of lines that follow, one {@code white black} line per board in board
 * order, and the PAB as {@code id 0} last. Ids are the file's own; lines end with LF.
 */
final class PairingReply {

    private PairingReply() {}

    static String of(RoundPairing pairing) {
        var reply = new StringBuilder();
        var lines = pairing.boards().size() + (pairing.pairingAllocatedBye().isPresent() ? 1 : 0);
        reply.append(lines).append('\n');
        pairing.boards()
                .forEach(board -> reply.append(board.white())
                        .append(' ')
                        .append(board.black())
                        .append('\n'));
        pairing.pairingAllocatedBye()
                .ifPresent(participant -> reply.append(participant).append(" 0\n"));
        return reply.toString();
    }
}
