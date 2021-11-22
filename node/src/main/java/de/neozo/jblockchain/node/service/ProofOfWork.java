package de.neozo.jblockchain.node.service;

import de.neozo.jblockchain.common.domain.Block;

public class ProofOfWork {
    /**
     * Count the number of bytes in the hash, which are zero at the beginning
     *
     * @return int number of leading zeros
     * @param block the Block to analyze
     */
    public static int getLeadingZerosCount(Block block) {
        var count = 0;
        while (count < block.getHash().length) {
            if (block.getHash()[count] != 0) return count;
            count++;
        }
        return count;
    }
}
