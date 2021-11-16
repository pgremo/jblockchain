package de.neozo.jblockchain.common.domain;


import de.neozo.jblockchain.common.Hashes;

import java.time.Clock;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static de.neozo.jblockchain.common.Bytes.toByteArray;
import static java.util.stream.Collectors.toCollection;

public class Block {

    /**
     * Unique identifier which can be generated by hashing previousBlockHash, merkleRoot, tries and timestamp
     */
    private final byte[] hash;

    /**
     * Hash of previous block in chain
     */
    private final byte[] previousHash;

    /**
     * List of Transaction which are part of this Block
     */
    private final List<Transaction> transactions;

    /**
     * Hash of all Transaction hashes, calculated in a tree-like manner
     */
    private final byte[] merkleRoot;

    /**
     * Self-chosen number to manipulate the Block hash
     */
    private final long nonce;

    /**
     * Creation time of this Block
     */
    private final long timestamp;

    public Block(byte[] previousHash, List<Transaction> transactions, long nonce, Clock clock) {
        this.previousHash = Objects.requireNonNullElseGet(previousHash, () -> new byte[0]);
        this.transactions = transactions;
        this.nonce = nonce;
        this.timestamp = clock.millis();
        this.merkleRoot = calculateMerkleRoot();
        this.hash = calculateHash();
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getPreviousHash() {
        return previousHash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public byte[] getMerkleRoot() {
        return merkleRoot;
    }

    public long getNonce() {
        return nonce;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Calculates the hash using relevant fields of this type
     *
     * @return SHA256-hash as raw bytes
     */
    public byte[] calculateHash() {
        return Hashes.digest(
                previousHash,
                toByteArray(nonce),
                toByteArray(timestamp)
        );
    }

    /**
     * Calculates the Hash of all transactions as hash tree.
     * https://en.wikipedia.org/wiki/Merkle_tree
     *
     * @return SHA256-hash as raw bytes
     */
    public byte[] calculateMerkleRoot() {
        var hashQueue = transactions.stream().map(Transaction::getHash).collect(toCollection(LinkedList::new));
        while (hashQueue.size() > 1) {
            hashQueue.add(Hashes.digest(hashQueue.poll(), hashQueue.poll()));
        }
        return hashQueue.poll();
    }

    /**
     * Count the number of bytes in the hash, which are zero at the beginning
     *
     * @return int number of leading zeros
     */
    public int getLeadingZerosCount() {
        var count = 0;
        while (count < hash.length) {
            if (hash[count] != 0) return count;
            count++;
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Block block = (Block) o;

        return Arrays.equals(hash, block.hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }
}
