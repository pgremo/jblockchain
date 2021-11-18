package de.neozo.jblockchain.node.rest;


import de.neozo.jblockchain.common.domain.Transaction;
import de.neozo.jblockchain.node.service.NodeService;
import de.neozo.jblockchain.node.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


@RestController()
@RequestMapping("transaction")
public class TransactionController {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;
    private final NodeService nodeService;

    @Autowired
    public TransactionController(TransactionService transactionService, NodeService nodeService) {
        this.transactionService = transactionService;
        this.nodeService = nodeService;
    }

    /**
     * Retrieve all Transactions, which aren't in a block yet
     *
     * @return JSON list of Transactions
     */
    @GetMapping
    Set<Transaction> getTransactionPool() {
        return transactionService.getTransactionPool();
    }


    /**
     * Add a new Transaction to the pool.
     * It is expected that the transaction has a valid signature and the correct hash.
     *
     * @param transaction the Transaction to add
     * @param publish     if true, this Node is going to inform all other Nodes about the new Transaction
     */
    @PutMapping
    void addTransaction(@RequestBody Transaction transaction, @RequestParam(required = false) Boolean publish) {
        LOG.info("Add transaction " + Base64.getEncoder().encodeToString(transaction.getHash()));
        var success = transactionService.add(transaction);

        if (!success) throw new ResponseStatusException(BAD_REQUEST);

        if (publish != null && publish) {
            nodeService.broadcastPut("transaction", transaction);
        }
    }

}
