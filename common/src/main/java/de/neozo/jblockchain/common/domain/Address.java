package de.neozo.jblockchain.common.domain;

import de.neozo.jblockchain.common.Hashes;

import java.util.Arrays;

public class Address {

    /**
     * Unique identifier which can be generated by hashing name and publicKey
     */
    private final byte[] hash;

    /**
     * Self-given name for this Address
     */
    private final String name;

    /**
     * The public key for this Address to ensure everybody is able to verify signed messages
     */
    private final byte[] publicKey;

    public Address(String name, byte[] publicKey) {
        this.name = name;
        this.publicKey = publicKey;
        this.hash = calculateHash();
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }

    /**
     * Calculates the hash using relevant fields of this type
     *
     * @return SHA256-hash as raw bytes
     */
    private byte[] calculateHash() {
        return Hashes.digest(name.getBytes(), publicKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var address = (Address) o;

        return Arrays.equals(hash, address.hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }
}
