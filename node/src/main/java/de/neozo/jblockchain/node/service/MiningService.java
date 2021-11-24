package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.domain.Block;
import de.neozo.jblockchain.common.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static de.neozo.jblockchain.node.service.ProofOfWork.getLeadingZerosCount;
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
            var transactions = transactionService.getTransactionPool()
                    .limit(maxTransactionsPerBlock).collect(toList());

            if (!transactions.isEmpty()) {
                mine(transactions)
                        .ifPresent(x -> {
                            LOG.info("Mined block with {} transactions and nonce {}", x.getTransactions().count(), x.getNonce());
                            blockService.append(x);
                            nodeService.broadcastPut("block", x);
                        });
            } else {
                LOG.info("No transactions available, pausing");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    LOG.error("Thread interrupted", e);
                }
            }
        }
        LOG.info("Miner stopped");
    }

    private Optional<Block> mine(List<Transaction> transactions) {
        var previousHash = blockService.getLastHash();
        var nonce = new AtomicLong(0L);
        return Stream
                .generate(() -> new Block(
                        previousHash,
                        transactions,
                        difficulty,
                        nonce.getAndIncrement(),
                        clock.millis()
                ))
                .filter(x -> runMiner.get())
                .filter(x -> getLeadingZerosCount(x) >= difficulty)
                .findFirst();
    }
}
