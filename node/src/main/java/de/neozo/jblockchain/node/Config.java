package de.neozo.jblockchain.node;


import java.net.MalformedURLException;
import java.net.URL;

public abstract class Config {

    /**
     * Address of a Node to use for initialization
     */
    public static final URL MASTER_NODE_ADDRESS;

    /**
     * Minimum number of leading zeros every block hash has to fulfill
     */
    public static final int DIFFICULTY = 3;

    /**
     * Maximum numver of Transactions a Block can hold
     */
    public static final int MAX_TRANSACTIONS_PER_BLOCK = 5;

    static {
        try {
            MASTER_NODE_ADDRESS = new URL("http://localhost:8080");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
