package de.neozo.jblockchain.common.domain;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

public class Block {

    /**
     * Unique identifier which can be generated by hashing previousBlockHash, merkleRoot, tries and timestamp
     */
    private byte[] hash;

    /**
     * Hash of previous block in chain
     */
    private byte[] previousBlockHash;

    /**
     * List of Transaction which are part of this Block
     */
    private List<Transaction> transactions;

    /**
     * Hash of all Transaction hashes, calculated in a tree-like manner
     */
    private byte[] merkleRoot;

    /**
     * Self-chosen number to manipulate the Block hash
     */
    private long tries;

    /**
     * Creation time of this Block
     */
    private long timestamp;

    public Block() {
    }

    public Block(byte[] previousBlockHash, List<Transaction> transactions, long tries) {
        this.previousBlockHash = previousBlockHash;
        this.transactions = transactions;
        this.tries = tries;
        this.timestamp = System.currentTimeMillis();
        this.merkleRoot = calculateMerkleRoot();
        this.hash = calculateHash();
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void setPreviousBlockHash(byte[] previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public byte[] getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(byte[] merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public long getTries() {
        return tries;
    }

    public void setTries(long tries) {
        this.tries = tries;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Calculates the hash using relevant fields of this type
     * @return SHA256-hash as raw bytes
     */
    public byte[] calculateHash() {
        var hashableData = ArrayUtils.addAll(previousBlockHash, merkleRoot);
        hashableData = ArrayUtils.addAll(hashableData, Transaction.toByteArray(tries));
        hashableData = ArrayUtils.addAll(hashableData, Transaction.toByteArray(timestamp));
        return DigestUtils.sha256(hashableData);
    }

    /**
     * Calculates the Hash of all transactions as hash tree.
     * https://en.wikipedia.org/wiki/Merkle_tree
     * @return SHA256-hash as raw bytes
     */
    public byte[] calculateMerkleRoot() {
        var hashQueue = transactions.stream().map(Transaction::getHash).collect(toCollection(LinkedList::new));
        while (hashQueue.size() > 1) {
            // take 2 hashes from queue
            var hashableData = ArrayUtils.addAll(hashQueue.poll(), hashQueue.poll());
            // put new hash at end of queue
            hashQueue.add(DigestUtils.sha256(hashableData));
        }
        return hashQueue.poll();
    }

    /**
     * Count the number of bytes in the hash, which are zero at the beginning
     * @return int number of leading zeros
     */
    public int getLeadingZerosCount() {
        for (var i = 0; i < getHash().length; i++) {
            if (getHash()[i] != 0) {
                return i;
            }
        }
        return getHash().length;
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
