package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.Signatures;
import de.neozo.jblockchain.common.domain.Node;
import de.neozo.jblockchain.common.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Stream;


@Service
public class TransactionService {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    private final AddressService addressService;


    /**
     * Pool of Transactions which are not included in a Block yet.
     */
    private final Set<Transaction> transactionPool = new HashSet<>();

    @Autowired
    public TransactionService(AddressService addressService) {
        this.addressService = addressService;
    }


    public Stream<Transaction> getTransactionPool() {
        return transactionPool.stream();
    }

    /**
     * Add a new Transaction to the pool
     * @param transaction Transaction to add
     * @return true if verifcation succeeds and Transaction was added
     */
    public synchronized boolean add(Transaction transaction) {
        if (!verify(transaction)) return false;
        transactionPool.add(transaction);
        return true;
    }

    /**
     * Remove Transaction from pool
     * @param transaction Transaction to remove
     */
    public void remove(Transaction transaction) {
        transactionPool.remove(transaction);
    }

    /**
     * Does the pool contain all given Transactions?
     * @param transactions Collection of Transactions to check
     * @return true if all Transactions are member of the pool
     */
    public boolean containsAll(Stream<Transaction> transactions) {
        return transactions.allMatch(transactionPool::contains);
    }

    private boolean verify(Transaction transaction) {
        // correct signature
        var sender = addressService.getByHash(transaction.getSender());
        if (sender == null) {
            LOG.warn("Unknown address {}", Base64.getEncoder().encodeToString(transaction.getSender()));
            return false;
        }

        try {
            if (!Signatures.verify(transaction.getPayload(), transaction.getSignature(), sender.getPublicKey())) {
                LOG.warn("Invalid signature");
                return false;
            }
        } catch (Exception e) {
            LOG.error("Error while verification", e);
            return false;
        }

        // correct hash
        if (!Arrays.equals(transaction.getHash(), transaction.calculateHash())) {
            LOG.warn("Invalid hash");
            return false;
        }

        return true;
    }

    /**
     * Download Transactions from other Node and them to the pool
     * @param node Node to query
     * @param restTemplate RestTemplate to use
     */
    public void retrieveTransactions(Node node, RestTemplate restTemplate) {
        var transactions = restTemplate.getForObject(node.address() + "/transaction", Transaction[].class);
        if (transactions == null) transactions = new Transaction[0];
        Collections.addAll(transactionPool, transactions);
        LOG.info("Retrieved {} transactions from node {}", transactions.length, node.address());
    }
}
