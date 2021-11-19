package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.domain.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toList;

@Service
public class MiningService implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(MiningService.class);

    @Value("${difficulty}")
    public int difficulty;

    @Value("${max-transactions-per-block}")
    public int maxTransactionsPerBlock;

    private final TransactionService transactionService;
    private final NodeService nodeService;
    private final BlockService blockService;

    private final AtomicBoolean runMiner = new AtomicBoolean(false);
    private final Clock clock;


    @Autowired
    public MiningService(TransactionService transactionService, NodeService nodeService, BlockService blockService, Clock clock) {
        this.transactionService = transactionService;
        this.nodeService = nodeService;
        this.blockService = blockService;
        this.clock = clock;
    }

    /**
     * Start the miner
     */
    public void startMiner() {
        if (runMiner.compareAndSet(false, true)) {
            LOG.info("Starting miner");
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Stop the miner after next iteration
     */
    public void stopMiner() {
        LOG.info("Stopping miner");
        runMiner.set(false);
    }

    /**
     * Loop for new blocks until someone signals to stop
     */
    @Override
    public void run() {
        while (runMiner.get()) {
            var block = mineBlock();
            if (block != null) {
                // Found block! Append and publish
                LOG.info("Mined block with {} transactions and nonce {}", block.getTransactions().size(), block.getNonce());
                blockService.append(block);
                nodeService.broadcastPut("block", block);
            }
        }
        LOG.info("Miner stopped");
    }

    private Block mineBlock() {
        // get previous hash and transactions
        var transactions = transactionService.getTransactionPool()
                .limit(maxTransactionsPerBlock).collect(toList());

        // sleep if no more transactions left
        if (transactions.isEmpty()) {
            LOG.info("No transactions available, pausing");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LOG.error("Thread interrupted", e);
            }
            return null;
        }

        // try new block until difficulty is sufficient
        var previousHash = blockService.getLastBlock() == null ? null : blockService.getLastBlock().getHash();
        for (var tries = 0L; runMiner.get(); tries++) {
            var block = new Block(
                    previousHash,
                    transactions,
                    difficulty,
                    tries,
                    clock.millis()
            );
            if (block.getLeadingZerosCount() >= difficulty) return block;
        }
        return null;
    }

}
